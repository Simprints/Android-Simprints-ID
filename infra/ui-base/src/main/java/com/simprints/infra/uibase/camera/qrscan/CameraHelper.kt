package com.simprints.infra.uibase.camera.qrscan

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This is an injectable wrapper around cameraX and ML kit APIs. There is no business logic.",
)
class CameraHelper @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
    private val cameraFocusManagerFactory: CameraFocusManager.Factory,
) {
    private val cameraFocusManager by lazy { cameraFocusManagerFactory.create(crashReportTag) }
    private var cameraProvider: ProcessCameraProvider? = null

    @AssistedFactory
    interface Factory {
        fun create(crashReportTag: LoggingConstants.CrashReportTag): CameraHelper
    }

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        cameraPreview: PreviewView,
        qrAnalyser: QrCodeAnalyzer,
        initializationErrorListener: CameraInitializationErrorListener,
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                val provider = providerFuture.get().also { provider ->
                    cameraProvider = provider
                    provider.unbindAll()
                }

                // Check if the back camera is available
                if (provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA).not()) {
                    initializationErrorListener.onCameraError()
                    return@addListener
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val analyzer = buildAnalyser(qrAnalyser)
                val preview = buildPreview(cameraPreview)

                try {
                    provider
                        .bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            analyzer,
                            preview,
                        ).let {
                            with(cameraFocusManager) {
                                setUpFocusOnTap(cameraPreview, it)
                                setUpAutoFocus(cameraPreview, it)
                            }
                        }
                } catch (e: Exception) {
                    Simber.i("Camera is already in use by another process", e, tag = crashReportTag)
                    initializationErrorListener.onCameraError()
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
    }

    private fun buildAnalyser(qrAnalyser: QrCodeAnalyzer) = ImageAnalysis
        .Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()
        .apply { setAnalyzer(Executors.newSingleThreadExecutor(), qrAnalyser) }

    private fun buildPreview(previewView: PreviewView): Preview = Preview
        .Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()
        .apply { setSurfaceProvider(previewView.surfaceProvider) }
}

fun interface CameraInitializationErrorListener {
    fun onCameraError()
}
