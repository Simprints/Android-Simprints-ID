package com.simprints.fingerprint.activities.launch

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.launch.request.LaunchTaskRequest
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.tools.extensions.getUcVersionString
import com.simprints.fingerprintscanner.ScannerUtils.convertAddressToSerial
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class LaunchPresenter(private val view: LaunchContract.View,
                      private val launchRequest: LaunchTaskRequest,
                      private val crashReportManager: FingerprintCrashReportManager,
                      private val scannerManager: ScannerManager,
                      private val timeHelper: FingerprintTimeHelper,
                      private val sessionEventsManager: FingerprintSessionEventsManager,
                      private val preferencesManager: FingerprintPreferencesManager,
                      private val analyticsManager: FingerprintAnalyticsManager) : LaunchContract.Presenter {

    private var setupFlow: Disposable? = null

    override fun start() {
        view.setLanguage(launchRequest.language)

        startSetup()
    }

    @SuppressLint("CheckResult")
    private fun startSetup() {
        setupFlow?.dispose()
        setupFlow = disconnectVero()
            .andThen(checkIfBluetoothIsEnabled())
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(resetVeroUI())
            .andThen(wakeUpVero())
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = {
                handleSetupFinished()
            })
    }

    private fun disconnectVero() =
        veroTask(15, R.string.launch_bt_connect, scannerManager.disconnectVero()).doOnComplete {
            logMessageForCrashReport("ScannerManager: disconnect")
        }

    private fun checkIfBluetoothIsEnabled() =
        veroTask(30, R.string.launch_bt_connect, scannerManager.checkBluetoothStatus()).doOnComplete {
            logMessageForCrashReport("ScannerManager: bluetooth is enabled")
        }

    private fun initVero() =
        veroTask(45, R.string.launch_bt_connect, scannerManager.initVero()).doOnComplete {
            logMessageForCrashReport("ScannerManager: init vero")
        }

    private fun connectToVero() =
        veroTask(60, R.string.launch_bt_connect, scannerManager.connectToVero()) { addBluetoothConnectivityEvent() }.doOnComplete {
            logMessageForCrashReport("ScannerManager: connectToVero")
        }

    private fun resetVeroUI() =
        veroTask(75, R.string.launch_setup, scannerManager.resetVeroUI()).doOnComplete {
            logMessageForCrashReport("ScannerManager: resetVeroUI")
        }

    private fun wakeUpVero() =
        veroTask(90, R.string.launch_wake_un20, scannerManager.wakeUpVero()) { updateBluetoothConnectivityEventWithVeroInfo() }.doOnComplete {
            logMessageForCrashReport("ScannerManager: wakeUpVero")
        }

    private fun updateBluetoothConnectivityEventWithVeroInfo() {
        scannerManager.scanner?.let {
            sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(it.getUcVersionString())
        }
    }

    private fun veroTask(progress: Int, @StringRes messageRes: Int, task: Completable, callback: (() -> Unit)? = null): Completable =
        Completable.fromAction { view.handleSetupProgress(progress, messageRes) }
            .andThen(task)
            .andThen(Completable.fromAction { callback?.invoke() })
            .doOnError { manageVeroErrors(it) }

    private fun manageVeroErrors(it: Throwable) {
        it.printStackTrace()
        launchScannerAlertOrShowDialog(scannerManager.getAlertType(it))
        crashReportManager.logExceptionOrSafeException(it)
    }

    private fun launchScannerAlertOrShowDialog(alert: FingerprintAlert) {
        if (alert == FingerprintAlert.DISCONNECTED) {
            view.showDialogForScannerErrorConfirmation(scannerManager.lastPairedScannerId ?: "")
        } else {
            launchAlert(alert)
        }
    }

    private fun handleSetupFinished() {
        view.handleSetupProgress(100, R.string.launch_finished)
        view.doVibrate()
        scannerManager.scanner?.let {
            preferencesManager.lastScannerUsed = convertAddressToSerial(it.macAddress)
            preferencesManager.lastScannerVersion = it.hardwareVersion.toString()
            analyticsManager.logScannerProperties(it.macAddress ?: "", it.scannerId ?: "")
        }
        view.continueToNextActivity()
    }

    override fun handleOnBackPressed() {
        handleOnBackOrDeclinePressed()
    }

    override fun handleDeclinePressed() {
        handleOnBackOrDeclinePressed()
    }

    private fun handleOnBackOrDeclinePressed() {
        view.goToRefusalActivity()
    }

    override fun tryAgainFromErrorOrRefusal() {
        setupFlow?.dispose()
        view.dismissScannerErrorConfirmationDialog()
        startSetup()
    }

    private fun launchAlert(alert: FingerprintAlert) {
        view.doLaunchAlert(alert)
    }

    override fun handleScannerDisconnectedYesClick() {
        launchAlert(FingerprintAlert.DISCONNECTED)
    }

    override fun handleScannerDisconnectedNoClick() {
        launchAlert(FingerprintAlert.NOT_PAIRED)
    }

    private fun addBluetoothConnectivityEvent() {
        scannerManager.scanner?.let {
            sessionEventsManager.addEventInBackground(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerInfo(
                        it.scannerId ?: "",
                        it.macAddress,
                        it.getUcVersionString())))
        }
    }

    private fun logMessageForCrashReport(message: String) {
        crashReportManager.logMessageForCrashReport(
            FingerprintCrashReportTag.SCANNER_SETUP,
            FingerprintCrashReportTrigger.SCANNER, message = message)
    }

    override fun logScannerErrorDialogShownToCrashReport() {
        crashReportManager.logMessageForCrashReport(
            FingerprintCrashReportTag.ALERT,
            FingerprintCrashReportTrigger.SCANNER,
            message = "Scanner error confirm dialog shown")
    }
}
