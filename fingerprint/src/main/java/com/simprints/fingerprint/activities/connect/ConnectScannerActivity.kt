package com.simprints.fingerprint.activities.connect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import com.simprints.core.tools.extentions.requestPermissionsIfRequired
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel.BackButtonBehaviour.*
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForConnectScannerActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.infra.logging.Simber
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConnectScannerActivity : FingerprintActivity() {

    private val permissionCode = 0
    private val viewModel: ConnectScannerViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val connectScannerRequest: ConnectScannerTaskRequest =
            this.intent.extras?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()

        viewModel.launchAlert.activityObserveEventWith { launchAlert(this, it) }
        viewModel.finish.activityObserveEventWith { vibrateAndContinueToNextActivity() }
        viewModel.finishAfterError.activityObserveEventWith { finishWithError() }
        viewModel.init(connectScannerRequest.connectMode)

        if (Build.VERSION.SDK_INT < 31)
            viewModel.start()
        else if (!requestPermissionsIfRequired(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ), permissionCode
            )
        )
            viewModel.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Simber.i("Bluetooth permission was accepted")
            viewModel.start()
        } else {
            Simber.w("Bluetooth permission was denied")
            finishWithError()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> viewModel.retryConnect()
            }
        }
    }

    private fun vibrateAndContinueToNextActivity() {
        Vibrate.vibrate(this)
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())
        })
    }

    private fun finishWithError() {
        setResultAndFinish(ResultCode.ALERT, Intent().apply {
            putExtra(
                AlertTaskResult.BUNDLE_KEY,
                AlertTaskResult(
                    FingerprintAlert.UNEXPECTED_ERROR,
                    AlertTaskResult.CloseButtonAction.CLOSE
                )
            )
        })
    }

    private fun goToRefusalActivity() {
        startActivityForResult(Intent(this, RefusalActivity::class.java), RequestCode.REFUSAL.value)
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun onBackPressed() {
        when (viewModel.backButtonBehaviour.value) {
            DISABLED -> { /* Do nothing */
            }
            EXIT_FORM, null -> goToRefusalActivity()
            EXIT_WITH_ERROR -> finishWithError()
        }
    }
}
