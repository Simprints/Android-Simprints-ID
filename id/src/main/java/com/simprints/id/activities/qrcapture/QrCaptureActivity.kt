package com.simprints.id.activities.qrcapture

import android.Manifest.permission.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.qrcapture.tools.CameraHelper
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.databinding.ActivityQrCaptureBinding
import com.simprints.id.tools.extensions.hasPermission
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrCaptureActivity : BaseSplitActivity() {

    @Inject lateinit var cameraHelper: CameraHelper
    @Inject lateinit var qrCodeProducer: QrCodeProducer
    private val binding by viewBinding(ActivityQrCaptureBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        setContentView(R.layout.activity_qr_capture)

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

    private fun startCamera() {
        lifecycleScope.launch {
            cameraHelper.startCamera(
                this@QrCaptureActivity,
                binding.cameraPreview,
                qrCodeProducer
            )

            addFocusDrawable()

            val qrCode = qrCodeProducer.qrCodeChannel.receive()
            sendQrCodeInResultIfNotEmpty(qrCode)
        }
    }

    private fun addFocusDrawable() {
        val img = ImageView(this@QrCaptureActivity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.gravity = Gravity.CENTER
            }
            setImageResource(R.drawable.ic_camera_focus)
            translationZ = 1f
        }
        binding.previewRoot.addView(img)
    }

    private fun sendQrCodeInResultIfNotEmpty(qrCodeValue: String) {
        if (qrCodeValue.isNotEmpty()) {
            val data = Intent().putExtra(QR_RESULT_KEY, qrCodeValue)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    companion object {
        const val QR_RESULT_KEY = "SCAN_RESULT"

        private const val REQUEST_CODE_CAMERA = 100

        fun getIntent(context: Context) = Intent(context, QrCaptureActivity::class.java)
    }

}
