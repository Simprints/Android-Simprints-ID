package com.simprints.id.activities.qrcapture

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Matrix
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.qrcapture.tools.QrCaptureHelper
import com.simprints.id.activities.qrcapture.tools.QrCaptureListener
import com.simprints.id.tools.extensions.hasPermission
import kotlinx.android.synthetic.main.activity_qr_capture.*
import javax.inject.Inject

class QrCaptureActivity : AppCompatActivity(R.layout.activity_qr_capture), QrCaptureListener {

    @Inject lateinit var qrCaptureHelper: QrCaptureHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
    }

    override fun onResume() {
        super.onResume()

        if (hasPermission(CAMERA))
            startCamera()
        else
            requestPermissions(arrayOf(CAMERA), REQUEST_CODE_CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA && grantResults.all { it == PERMISSION_GRANTED }) {
            startCamera()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onQrCodeCaptured(qrCodeValue: String) {
        val data = Intent().putExtra(QR_RESULT_KEY, qrCodeValue)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun startCamera() {
        val preview = qrCaptureHelper.buildPreview().apply {
            setOnPreviewOutputUpdateListener { previewOutput ->
                with(qrCaptureRoot) {
                    removeView(cameraPreview)
                    addView(cameraPreview, 0)
                }

                with(cameraPreview) {
                    surfaceTexture = previewOutput.surfaceTexture
                    updateTransform()
                }
            }
        }

        val useCase = qrCaptureHelper.buildUseCase(qrCaptureListener = this)

        CameraX.bindToLifecycle(this, preview, useCase)
    }

    private fun TextureView.updateTransform() {
        val centreX = x / 2
        val centreY = y / 2

        val rotationDegrees = when (display.rotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> return
        }

        val matrix = Matrix().apply {
            postRotate(-rotationDegrees, centreX, centreY)
        }

        setTransform(matrix)
    }

    companion object {
        const val QR_RESULT_KEY = "SCAN_RESULT"

        private const val REQUEST_CODE_CAMERA = 100

        fun getIntent(context: Context) = Intent(context, QrCaptureActivity::class.java)
    }

}
