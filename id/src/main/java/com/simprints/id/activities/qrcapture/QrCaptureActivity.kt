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
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.R
import com.simprints.id.activities.qrcapture.tools.CameraHelper
import com.simprints.id.activities.qrcapture.tools.QrCodeProducer
import com.simprints.id.databinding.ActivityQrCaptureBinding
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_ERROR_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.Companion.QR_SCAN_RESULT_KEY
import com.simprints.id.tools.InternalConstants.QrCapture.QrCaptureError.CAMERA_NOT_AVAILABLE
import com.simprints.id.tools.InternalConstants.QrCapture.QrCaptureError.PERMISSION_NOT_GRANTED
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QrCaptureActivity : BaseSplitActivity() {

    @Inject
    lateinit var cameraHelper: CameraHelper
    @Inject
    lateinit var qrCodeProducer: QrCodeProducer

    private val binding by viewBinding(ActivityQrCaptureBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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
            val data = Intent().putExtra(QR_SCAN_ERROR_KEY, PERMISSION_NOT_GRANTED)
            setResult(Activity.RESULT_CANCELED, data)
            finish()
        }
    }

    private fun startCamera() {
        lifecycleScope.launch {
            try {
                cameraHelper.startCamera(
                    this@QrCaptureActivity,
                    binding.cameraPreview,
                    qrCodeProducer
                )

                addFocusDrawable()

                val qrCode = qrCodeProducer.qrCodeChannel.receive()
                sendQrCodeInResultIfNotEmpty(qrCode)
            } catch (e: Exception) {
                if (!isFinishing) {
                    Simber.e(e)
                    val data = Intent().putExtra(QR_SCAN_ERROR_KEY, CAMERA_NOT_AVAILABLE)
                    setResult(Activity.RESULT_CANCELED, data)
                    finish()
                }
            }
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
            val data = Intent().putExtra(QR_SCAN_RESULT_KEY, qrCodeValue)
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    companion object {
        private const val REQUEST_CODE_CAMERA = 100

        fun getIntent(context: Context) = Intent(context, QrCaptureActivity::class.java)
    }
}
