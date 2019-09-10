package com.simprints.fingerprint.activities.connect

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
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
import com.simprints.fingerprintscanner.v1.ScannerUtils.convertAddressToSerial
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

class ConnectScannerViewModel(private val crashReportManager: FingerprintCrashReportManager,
                              private val scannerManager: ScannerManager,
                              private val timeHelper: FingerprintTimeHelper,
                              private val sessionEventsManager: FingerprintSessionEventsManager,
                              private val preferencesManager: FingerprintPreferencesManager,
                              private val analyticsManager: FingerprintAnalyticsManager) : ViewModel() {

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val message: MutableLiveData<Int> = MutableLiveData(R.string.connect_scanner_bt_connect)
    val vibrate = MutableLiveData<Unit>()

    val launchAlert = MutableLiveData<FingerprintAlert>()
    val finish = MutableLiveData<Unit>()

    val showScannerErrorDialogWithScannerId = MutableLiveData<String>()

    private var setupFlow: Disposable? = null

    fun start() {
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
        veroTask(computeProgress(1), R.string.connect_scanner_bt_connect, "ScannerManager: disconnect",
            scannerManager.disconnectVero())

    private fun checkIfBluetoothIsEnabled() =
        veroTask(computeProgress(2), R.string.connect_scanner_bt_connect, "ScannerManager: bluetooth is enabled",
            scannerManager.checkBluetoothStatus())

    private fun initVero() =
        veroTask(computeProgress(3), R.string.connect_scanner_bt_connect, "ScannerManager: init vero",
            scannerManager.initVero())

    private fun connectToVero() =
        veroTask(computeProgress(4), R.string.connect_scanner_bt_connect, "ScannerManager: connectToVero",
            scannerManager.connectToVero()) { addBluetoothConnectivityEvent() }

    private fun resetVeroUI() =
        veroTask(computeProgress(5), R.string.connect_scanner_setup, "ScannerManager: resetVeroUI",
            scannerManager.resetVeroUI())

    private fun wakeUpVero() =
        veroTask(computeProgress(6), R.string.connect_scanner_wake_un20, "ScannerManager: wakeUpVero",
            scannerManager.wakeUpVero()) { updateBluetoothConnectivityEventWithVeroInfo() }

    private fun updateBluetoothConnectivityEventWithVeroInfo() {
        scannerManager.scanner?.let {
            sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(it.getUcVersionString())
        }
    }

    private fun veroTask(progress: Int, @StringRes messageRes: Int, crashReportMessage: String,
                         task: Completable, callback: (() -> Unit)? = null): Completable =
        Completable.fromAction {
            this.progress.postValue(progress)
            this.message.postValue(messageRes)
        }
            .andThen(task)
            .andThen(Completable.fromAction { callback?.invoke() })
            .doOnError { manageVeroErrors(it) }
            .doOnComplete {
                logMessageForCrashReport(crashReportMessage)
            }

    private fun manageVeroErrors(it: Throwable) {
        it.printStackTrace()
        launchScannerAlertOrShowDialog(scannerManager.getAlertType(it))
        crashReportManager.logExceptionOrSafeException(it)
    }

    private fun launchScannerAlertOrShowDialog(alert: FingerprintAlert) {
        if (alert == FingerprintAlert.DISCONNECTED) {
            showScannerErrorDialogWithScannerId.postValue(scannerManager.lastPairedScannerId ?: "")
        } else {
            launchAlert.postValue(alert)
        }
    }

    private fun handleSetupFinished() {
        progress.postValue(computeProgress(7))
        message.postValue(R.string.connect_scanner_finished)
        vibrate.postValue(Unit)
        scannerManager.scanner?.let {
            preferencesManager.lastScannerUsed = convertAddressToSerial(it.macAddress)
            preferencesManager.lastScannerVersion = it.hardwareVersion.toString()
            analyticsManager.logScannerProperties(it.macAddress ?: "", it.scannerId ?: "")
        }
        finish.postValue(Unit)
    }

    fun tryAgainFromErrorOrRefusal() {
        startSetup()
    }

    fun handleScannerDisconnectedYesClick() {
        launchAlert.postValue(FingerprintAlert.DISCONNECTED)
    }

    fun handleScannerDisconnectedNoClick() {
        launchAlert.postValue(FingerprintAlert.NOT_PAIRED)
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

    fun logScannerErrorDialogShownToCrashReport() {
        crashReportManager.logMessageForCrashReport(
            FingerprintCrashReportTag.ALERT,
            FingerprintCrashReportTrigger.SCANNER,
            message = "Scanner error confirm dialog shown")
    }

    override fun onCleared() {
        super.onCleared()
        setupFlow?.dispose()
    }

    companion object {
        private const val NUMBER_OF_STEPS = 7
        private fun computeProgress(step: Int) = step * 100 / NUMBER_OF_STEPS
    }
}
