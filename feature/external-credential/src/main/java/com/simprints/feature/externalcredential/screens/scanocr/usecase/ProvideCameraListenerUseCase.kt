package com.simprints.feature.externalcredential.screens.scanocr.usecase

import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject


internal class ProvideCameraListenerUseCase @Inject constructor() {
    operator fun invoke(
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        surfaceProvider: Preview.SurfaceProvider,
        viewLifecycleOwner: LifecycleOwner,
        onImageAnalysisReady: (ImageAnalysis) -> Unit,
    ): Runnable {
        return Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val aspectRatio = AspectRatio.RATIO_16_9
            val preview = Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build().also {
                    it.setSurfaceProvider(surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis)
                onImageAnalysisReady(imageAnalysis)
            } catch (e: Exception) {
                Simber.e("Camera binding failed in OCR", e, MULTI_FACTOR_ID)
            }
        }
    }
}
