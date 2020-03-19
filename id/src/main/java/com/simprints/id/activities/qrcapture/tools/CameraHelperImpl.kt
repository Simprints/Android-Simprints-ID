package com.simprints.id.activities.qrcapture.tools

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.work.await

class CameraHelperImpl(
    private val context: Context,
    private val previewBuilder: QrPreviewBuilder,
    private val cameraFocusManager: CameraFocusManager,
    private val qrCodeProducer: QrCodeProducer
) : CameraHelper {

    override suspend fun startCamera(lifecycleOwner: LifecycleOwner, cameraPreview: PreviewView) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        val preview = previewBuilder.buildPreview(cameraPreview)
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val useCase = qrCodeProducer.useCase

        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, useCase, preview).let {
            with(cameraFocusManager) {
                setUpFocusOnTap(cameraPreview, it)
                setUpAutoFocus(cameraPreview, it)
            }
        }
    }

}
