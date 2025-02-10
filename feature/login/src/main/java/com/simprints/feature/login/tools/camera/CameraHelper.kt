package com.simprints.feature.login.tools.camera

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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This is an injectable wrapper around cameraX and ML kit APIs. There is no business logic.",
)
internal class CameraHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cameraFocusManager: CameraFocusManager,
) {
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        cameraPreview: PreviewView,
        qrAnalyser: QrCodeAnalyzer,
        initializationErrorListener: CameraInitializationErrorListener,
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener(
            {
                val cameraProvider = providerFuture.get()
                // Check if the back camera is available
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA).not()) {
                    initializationErrorListener.onCameraError()
                    return@addListener
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                val analyzer = buildAnalyser(qrAnalyser)
                val preview = buildPreview(cameraPreview)

                try {
                    cameraProvider
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
                    Simber.i("Camera is already in use by another process", e, tag = LOGIN)
                    initializationErrorListener.onCameraError()
                }
            },
            ContextCompat.getMainExecutor(context),
        )
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
