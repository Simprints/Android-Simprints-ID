package com.simprints.fingerprint.activities.connect

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.tools.extentions.hasPermissions
import com.simprints.core.tools.extentions.permissionFromResult
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
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.system.Vibrate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectScannerActivity : FingerprintActivity() {

    private var shouldRequestPermissions = true

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )
    private val viewModel: ConnectScannerViewModel by viewModels()
    private val showRefusal = registerForActivityResult(ShowExitFormWrapper()) { result ->
        RefusalAlertHelper.handleRefusal(
            result = result,
            onBack = { shouldRequestPermissions = true },
            onSubmit = { setResultAndFinish(ResultCode.REFUSED, it) }
        )
    }

    private val alertHelper = AlertActivityHelper()
    private val showAlert = registerForActivityResult(ShowAlertWrapper()) { result ->
        alertHelper.handleAlertResult(
            this,
            result,
            showRefusal = { showRefusal.launch(RefusalAlertHelper.refusalArgs()) },
            retry = {}
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissionsCall = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val mappedPermissions = permissions
            .filterKeys(bluetoothPermissions::contains)
            .map { entry ->
                permissionFromResult(permission = entry.key, grantResult = entry.value)
            }
        val permission = when {
            mappedPermissions.contains(PermissionStatus.DeniedNeverAskAgain) -> PermissionStatus.DeniedNeverAskAgain
            mappedPermissions.contains(PermissionStatus.Denied) -> PermissionStatus.Denied
            else -> PermissionStatus.Granted
        }
        Simber.i("Bluetooth permission: $permission")
        viewModel.setBluetoothPermission(permission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val connectScannerRequest: ConnectScannerTaskRequest =
            this.intent.extras?.getParcelable(ConnectScannerTaskRequest.BUNDLE_KEY) as ConnectScannerTaskRequest?
                ?: throw InvalidRequestForConnectScannerActivityException()


        viewModel.launchAlert.activityObserveEventWith {
            showAlert.launch(it.toAlertConfig().toArgs())
        }
        viewModel.finish.activityObserveEventWith { vibrateAndContinueToNextActivity() }
        viewModel.finishAfterError.activityObserveEventWith { finishWithError() }
        viewModel.bluetoothPermission.activityObserveEventWith { permission ->
            when (permission) {
                PermissionStatus.Granted -> viewModel.start()
                PermissionStatus.Denied -> requestBluetoothPermissions()
                PermissionStatus.DeniedNeverAskAgain -> viewModel.handleNoBluetoothPermission()
            }
        }
        viewModel.init(connectScannerRequest.connectMode)
    }

    override fun onResume() {
        super.onResume()
        if (shouldRequestPermissions) {
            shouldRequestPermissions = false
            checkBluetoothPermissions()
        } else {
            alertHelper.handleResume { shouldRequestPermissions = true }
        }
    }

    private fun checkBluetoothPermissions() {
        if (hasBluetoothPermissions()) {
            viewModel.setBluetoothPermission(PermissionStatus.Granted)
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= 31) {
            bluetoothPermissionsCall.launch(bluetoothPermissions)
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < 31) true
        else hasPermissions(bluetoothPermissions)
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
