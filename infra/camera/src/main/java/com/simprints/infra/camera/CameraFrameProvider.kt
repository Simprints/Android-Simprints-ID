package com.simprints.infra.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import android.view.View
import android.widget.ImageView
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.simprints.core.DispatcherBG
import com.simprints.core.DispatcherMain
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.camera.helpers.CameraFocusHelper
import com.simprints.infra.camera.helpers.FrameEmissionHelper
import com.simprints.infra.camera.repository.InjectedImageCache
import com.simprints.infra.camera.usecase.InjectedImagePreProcessUseCase
import com.simprints.infra.camera.usecase.NormalizeHighResBitmapToPreviewUseCase
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(reason = "Camera API wrapper")
class CameraFrameProvider @Inject internal constructor(
    @ApplicationContext private val context: Context,
    @DispatcherBG private val bgDispatcher: CoroutineDispatcher,
    @DispatcherMain private val mainDispatcher: CoroutineDispatcher,
    private val cameraFocusManagerFactory: CameraFocusHelper.Factory,
    private val normalizeHighResBitmapToPreviewUseCase: NormalizeHighResBitmapToPreviewUseCase,
    private val injectedImageCache: InjectedImageCache,
    private val injectedImagePreProcessUseCase: InjectedImagePreProcessUseCase,
) {
    private var executor: ExecutorService = Executors.newSingleThreadExecutor()

    val frames: Flow<Frame>
        field = MutableSharedFlow<Frame>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null

    private lateinit var previewRect: Rect
    private lateinit var targetRect: Rect

    private var previewSurface: PreviewView? = null
    private var injectionOverlay: ImageView? = null

    private val frameEmissionHelper = FrameEmissionHelper()

    fun isInitialised() = camera != null

    /**
     * Initialise the camera and connect it to the provided UI elements.
     *
     * Call only after the preview and the targer has been laid out:
     * ```
     *   binding.preview.awaitLayout()
     *   binding.targerOverlay.awaitLayout()
     * ```
     */

    @ExcludedFromGeneratedTestCoverageReports(reason = "Camera API wrapper")
    suspend fun initialiseCamera(
        lifecycleOwner: LifecycleOwner,
        cameraPreviewView: CameraPreviewView,
        target: Rect? = null,
        highResolution: Boolean = false,
        onError: (Throwable) -> Unit = {},
    ) = withContext(bgDispatcher) {
        val previewView = cameraPreviewView.previewView
        try {
            previewRect = fullPreviewSizeRect(previewView)
            targetRect = target ?: previewRect
        } catch (e: Exception) {
            Simber.e("Preview and target calculation failed", e, tag = CrashReportTag.CAMERA)
            withContext(mainDispatcher) { onError(e) }
            return@withContext
        }

        ensureExecutor()
        previewSurface = previewView
        injectionOverlay = cameraPreviewView.injectionOverlay

        frameEmissionHelper.configure(highResolution = highResolution)

        val cameraSelector = DEFAULT_BACK_CAMERA
        val resolutionSelector = ResolutionSelector
            .Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(previewRect.width(), previewRect.height()),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                ),
            ).build()

        imageAnalysis = ImageAnalysis
            .Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageRotationEnabled(true)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalysis?.setAnalyzer(executor) { proxy ->
            proxy.use { frame ->
                when {
                    frameEmissionHelper.shouldEmitAnalyserFrame() -> emitFrame(frame.toBitmap(), frame.imageInfo.rotationDegrees)
                    frameEmissionHelper.beginHighResolutionCapture() -> captureHighResolutionFrame()
                }
            }
        }

        val capture = ImageCapture
            .Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
        imageCapture = capture

        withContext(mainDispatcher) {
            val preview = Preview
                .Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val provider = ProcessCameraProvider.awaitInstance(context).also {
                cameraProvider = it
            }

            try {
                provider.unbindAll()
                camera = provider
                    .bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        imageAnalysis,
                        capture,
                        preview,
                    ).also { boundCamera ->
                        with(cameraFocusManagerFactory.create(CrashReportTag.CAMERA)) {
                            setUpFocusOnTap(previewView, boundCamera)
                            setUpAutoFocus(previewView, boundCamera)
                        }
                    }
            } catch (e: Exception) {
                Simber.e("Camera binding failed", e, tag = CrashReportTag.CAMERA)
                onError(e)
            }
        }
    }

    /**
     * Toggle frame emission to save resources when processing of a
     * single frame requires significant amount of time.
     */
    fun setFrameEmissionEnabled(enabled: Boolean) {
        frameEmissionHelper.setFrameEmissionEnabled(enabled)
    }

    /**
     * Toggle camera flash.
     */
    fun setTorchEnabled(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
    }

    /**
     * Clear all resources and stop the camera.
     */
    fun release() {
        imageAnalysis?.clearAnalyzer()
        imageAnalysis = null

        cameraProvider?.unbindAll()
        cameraProvider = null

        previewSurface = null
        injectionOverlay = null
        imageCapture = null
        camera = null
        frameEmissionHelper.reset()

        if (!executor.isShutdown) executor.shutdown()
    }

    private fun ensureExecutor() {
        if (executor.isShutdown) {
            executor = Executors.newSingleThreadExecutor()
        }
    }

    private fun emitFrame(
        bitmap: Bitmap,
        rotation: Int,
    ) {
        val injected = injectedImageCache.injectedImage
        val (frameBitmap: Bitmap, frameRotation: Int) = when (injected) {
            null -> bitmap to rotation
            else -> injectedImagePreProcessUseCase(injected, previewRect, targetRect) to 0
        }
        if (injected != null) {
            displayInjectedImage(frameBitmap)
        }
        frames.tryEmit(
            Frame(
                bitmap = frameBitmap,
                rotation = frameRotation,
                previewBounds = previewRect,
                targetBounds = targetRect,
            ),
        )
    }

    private fun displayInjectedImage(bitmap: Bitmap) {
        val overlay = injectionOverlay
        overlay?.post {
            overlay.setImageBitmap(bitmap)
            overlay.visibility = View.VISIBLE
        }
    }

    private fun captureHighResolutionFrame() {
        val capture = imageCapture
        if (capture == null) {
            frameEmissionHelper.cancelHighResolutionCapture()
            return
        }

        try {
            capture.takePicture(
                executor,
                @ExcludedFromGeneratedTestCoverageReports(reason = "Camera API wrapper")
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        val shouldEmitFrame = frameEmissionHelper.completeHighResolutionCapture()
                        imageProxy.use { capturedFrame ->
                            val rotationDegrees = capturedFrame.imageInfo.rotationDegrees
                            val previewViewWidth = previewRect.width()
                            val previewViewHeight = previewRect.height()

                            if (shouldEmitFrame) {
                                emitFrame(
                                    normalizeHighResBitmapToPreviewUseCase(
                                        capturedFrame.toBitmap(),
                                        rotationDegrees,
                                        previewViewWidth,
                                        previewViewHeight,
                                    ),
                                    rotationDegrees,
                                )
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        frameEmissionHelper.cancelHighResolutionCapture()
                        Simber.e("High-res frame capture failed", exception, tag = CrashReportTag.CAMERA)
                    }
                },
            )
        } catch (e: Exception) {
            frameEmissionHelper.cancelHighResolutionCapture()
            Simber.e("High-res frame capture failed", e, tag = CrashReportTag.CAMERA)
        }
    }

    private fun fullPreviewSizeRect(surface: PreviewView): Rect = Rect(0, 0, surface.width, surface.height)
}
