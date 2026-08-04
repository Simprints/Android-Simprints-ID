package com.simprints.infra.camera

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.simprints.infra.camera.qrscan.CameraFocusManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class StandardCameraFrameProvider @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    private val cameraFocusManagerFactory: CameraFocusManager.Factory,
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
) : CameraFrameProvider {
    @AssistedFactory
    interface Factory : CameraFrameProvider.Factory {
        override fun create(crashReportTag: LoggingConstants.CrashReportTag): StandardCameraFrameProvider
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val _frames = MutableSharedFlow<Bitmap>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val frames: Flow<Bitmap> = _frames.asSharedFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    @Volatile
    private var injectedBitmap: Bitmap? = null
    private var receiverRegistered = false
    private var registrationContext: Context? = null
    private var surface: CameraPreviewView? = null
    private var frameSource: FrameSource = FrameSource.CAMERA
    private var emissionJob: Job? = null

    private val frameReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent,
        ) {
            val filename = intent.getStringExtra(EXTRA_FILE) ?: return
            val dir = context.getExternalFilesDir(null) ?: return
            val bitmap = BitmapFactory.decodeFile(File(dir, filename).absolutePath) ?: run {
                Simber.i("Frame injection: failed to decode '$filename'", tag = crashReportTag)
                return
            }
            val previousBitmap = injectedBitmap
            injectedBitmap = bitmap
            surface?.injectionOverlay?.let { overlay ->
                overlay.setImageBitmap(bitmap)
                overlay.visibility = View.VISIBLE
            }
            if (previousBitmap?.isRecycled == false) {
                previousBitmap.recycle()
            }
            frameSource = FrameSource.INJECTION
        }
    }

    override suspend fun bind(
        lifecycleOwner: LifecycleOwner,
        surface: CameraPreviewView,
    ) {
        this.surface = surface

        if (BuildConfig.DEBUG) {
            registrationContext = surface.context
            ContextCompat.registerReceiver(
                surface.context,
                frameReceiver,
                IntentFilter(ACTION_INJECT_FRAME),
                ContextCompat.RECEIVER_EXPORTED,
            )
            receiverRegistered = true
        }

        val imageAnalysis = ImageAnalysis
            .Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageRotationEnabled(true)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        imageAnalysis.setAnalyzer(executor) { proxy: ImageProxy ->
            proxy.use {
                if (frameSource == FrameSource.CAMERA) {
                    val bitmap = injectedBitmap ?: it.toBitmap()
                    _frames.tryEmit(bitmap)
                }
            }
        }

        emissionJob = lifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (frameSource == FrameSource.INJECTION) {
                    emitInjectedFrame()
                }
                delay(FRAME_INTERVAL_MS.milliseconds)
            }
        }

        val capture = ImageCapture
            .Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageCapture = capture

        val preview = Preview.Builder().build().also { it.surfaceProvider = surface.previewView.surfaceProvider }

        val provider = ProcessCameraProvider.awaitInstance(context)
        cameraProvider = provider
        try {
            provider.unbindAll()
            val boundCamera = provider.bindToLifecycle(
                lifecycleOwner,
                DEFAULT_BACK_CAMERA,
                imageAnalysis,
                capture,
                preview,
            )
            camera = boundCamera
            val focusManager = cameraFocusManagerFactory.create(crashReportTag)
            focusManager.setUpFocusOnTap(surface.previewView, boundCamera)
            focusManager.setUpAutoFocus(surface.previewView, boundCamera)
        } catch (e: Exception) {
            Simber.e("Camera binding failed", e, tag = crashReportTag)
        }
    }

    override suspend fun takePicture(): Bitmap? {
        injectedBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) {
                return bitmap.copy(
                    bitmap.config ?: Bitmap.Config.ARGB_8888,
                    false,
                )
            }
        }
        val capture = imageCapture ?: return null
        return suspendCancellableCoroutine { continuation ->
            capture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        continuation.resume(imageProxy.use { it.toBitmap() })
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Simber.e("High-res capture failed", exception, tag = crashReportTag)
                        continuation.resume(null)
                    }
                },
            )
        }
    }

    override fun setTorchEnabled(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    override fun release() {
        if (receiverRegistered) {
            registrationContext?.unregisterReceiver(frameReceiver)
            registrationContext = null
            receiverRegistered = false
        }
        emissionJob?.cancel()
        surface?.injectionOverlay?.visibility = View.GONE
        surface = null

        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        camera = null

        if (injectedBitmap?.isRecycled == false) {
            injectedBitmap?.recycle()
        }
        injectedBitmap = null
        if (!executor.isShutdown) executor.shutdown()
    }

    private fun emitInjectedFrame() {
        val source = injectedBitmap ?: return

        if (source.isRecycled) {
            return
        }

        // Bitmaps from camera might be recycled by the received. Sending a bitmap copy of the original for the latter to survive the
        // recycling process
        val frame = source.copy(
            source.config ?: Bitmap.Config.ARGB_8888,
            false,
        )

        _frames.tryEmit(frame)
    }

    private enum class FrameSource {
        CAMERA,
        INJECTION,
    }

    companion object {
        const val ACTION_INJECT_FRAME = "com.simprints.automation.INJECT_FRAME"
        private const val EXTRA_FILE = "frame"
        private const val FPS = 30
        private const val FRAME_INTERVAL_MS = 1000L / FPS
    }
}
