package com.simprints.fingerprint.activities.connect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.simprints.core.tools.extentions.requestPermissionsIfRequired
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ShowExitFormWrapper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.alert.result.AlertTaskResult
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel.BackButtonBehaviour.DISABLED
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel.BackButtonBehaviour.EXIT_FORM
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel.BackButtonBehaviour.EXIT_WITH_ERROR
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalAlertHelper
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForConnectScannerActivityException
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectScannerActivity : FingerprintActivity() {

    private val permissionCode = 0
    private val viewModel: ConnectScannerViewModel by viewModels()

    private val showRefusal = registerForActivityResult(ShowExitFormWrapper()) { result ->
        RefusalAlertHelper.handleRefusal(
            result = result,
            onBack = { viewModel.retryConnect() },
            onSubmit = { setResultAndFinish(ResultCode.REFUSED, it) },
        )
    }

    private val alertHelper = AlertActivityHelper()
    private val showAlert = registerForActivityResult(ShowAlertWrapper()) { result ->
        alertHelper.handleAlertResult(
            this,
            result,
            showRefusal = { showRefusal.launch(RefusalAlertHelper.refusalArgs()) },
            retry = { viewModel.retryConnect() },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val connectScannerRequest: ConnectScannerTaskRequest =
            this.intent.extras?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()

        viewModel.launchAlert.activityObserveEventWith { showAlert.launch(it.toAlertConfig().toArgs()) }
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

    override fun onResume() {
        super.onResume()
        alertHelper.handleResume { viewModel.retryConnect() }
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

    private fun vibrateAndContinueToNextActivity() {
        Vibrate.vibrate(this)
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(ConnectScannerTaskResult.BUNDLE_KEY, ConnectScannerTaskResult())
        })
    }

    private fun finishWithError(
        alertError: AlertError = AlertError.UNEXPECTED_ERROR,
    ) {
        setResultAndFinish(ResultCode.ALERT, Intent().apply {
            putExtra(AlertTaskResult.BUNDLE_KEY, AlertTaskResult(alertError))
        })
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun onBackPressed() {
        when (viewModel.backButtonBehaviour.value) {
            DISABLED -> { /* Do nothing */
            }

            EXIT_FORM, null -> showRefusal.launch(RefusalAlertHelper.refusalArgs())
            EXIT_WITH_ERROR -> finishWithError()
        }
    }
}
