package com.simprints.id.activities.login

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.simprints.id.R
import com.simprints.id.tools.extensions.hasPermission
import kotlinx.android.synthetic.main.activity_qr_capture.*
import java.util.concurrent.Executors

class QrCaptureActivity : AppCompatActivity(R.layout.activity_qr_capture),
    OnSuccessListener<List<FirebaseVisionBarcode>> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    override fun onSuccess(qrCodes: List<FirebaseVisionBarcode>?) {
        if (!qrCodes.isNullOrEmpty()) {
            val qrCode = qrCodes.first { !it.rawValue.isNullOrEmpty() }
            qrCode.rawValue?.let {
                val data = Intent().putExtra(QR_RESULT_KEY, it)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    private fun startCamera() {
        val resolution = Size(640, 480)
        val previewConfig = PreviewConfig.Builder()
            .setTargetResolution(resolution)
            .build()
        val preview = Preview(previewConfig).apply {
            setOnPreviewOutputUpdateListener {
                with(cameraPreview.parent as ViewGroup) {
                    removeView(cameraPreview)
                    addView(cameraPreview, 0)
                }

                cameraPreview.surfaceTexture = it.surfaceTexture
                updateTransform()
            }
        }

        val analysisConfig = ImageAnalysisConfig.Builder()
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val useCase = ImageAnalysis(analysisConfig).also {
            it.setAnalyzer(Executors.newSingleThreadExecutor(), QrCodeAnalyser(this))
        }

        CameraX.bindToLifecycle(this, preview, useCase)
    }

    private fun updateTransform() {
        val centreX = cameraPreview.x / 2
        val centreY = cameraPreview.y / 2

        val rotationDegrees = when (cameraPreview.display.rotation) {
            Surface.ROTATION_0 -> 0f
            Surface.ROTATION_90 -> 90f
            Surface.ROTATION_180 -> 180f
            Surface.ROTATION_270 -> 270f
            else -> return
        }

        val matrix = Matrix().apply {
            postRotate(-rotationDegrees, centreX, centreY)
        }

        cameraPreview.setTransform(matrix)
    }

    companion object {
        const val QR_RESULT_KEY = "SCAN_RESULT"

        private const val REQUEST_CODE_CAMERA = 100

        fun getIntent(context: Context) = Intent(context, QrCaptureActivity::class.java)
    }

}
