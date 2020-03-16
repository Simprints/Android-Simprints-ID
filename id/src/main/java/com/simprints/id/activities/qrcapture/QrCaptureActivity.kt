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
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.qrcapture.tools.CameraBinder
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.activities.qrcapture.tools.QrPreviewBuilder
import com.simprints.id.tools.extensions.hasPermission
import kotlinx.android.synthetic.main.activity_qr_capture.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrCaptureActivity : AppCompatActivity(R.layout.activity_qr_capture) {

    @Inject lateinit var cameraBinder: CameraBinder
    @Inject lateinit var qrPreviewBuilder: QrPreviewBuilder
    @Inject lateinit var qrCodeProducer: QrCodeProducer

    private lateinit var preview: Preview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)

        preview = buildPreview()

        if (hasPermission(CAMERA))
            startCamera()
    }

    override fun onResume() {
        super.onResume()

        if (!hasPermission(CAMERA))
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

    private fun buildPreview() = qrPreviewBuilder.buildPreview().apply {
        val qrCaptureRoot = cameraPreview.parent as ViewGroup
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

    private fun startCamera() {
        cameraBinder.bindToLifecycle(this, preview, qrCodeProducer.useCase)

        lifecycleScope.launch {
            val qrCode = qrCodeProducer.qrCodeChannel.receive()
            onQrCodeCaptured(qrCode)
        }
    }

    private fun onQrCodeCaptured(qrCodeValue: String) {
        val data = Intent().putExtra(QR_RESULT_KEY, qrCodeValue)
        setResult(Activity.RESULT_OK, data)
        finish()
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
