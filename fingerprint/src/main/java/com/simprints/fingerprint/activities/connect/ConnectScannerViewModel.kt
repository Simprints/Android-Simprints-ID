package com.simprints.fingerprint.activities.connect

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.tools.livedata.postEvent
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ConnectScannerViewModel(
    private val crashReportManager: FingerprintCrashReportManager,
    private val scannerManager: ScannerManager,
    private val timeHelper: FingerprintTimeHelper,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val preferencesManager: FingerprintPreferencesManager,
    private val analyticsManager: FingerprintAnalyticsManager,
    private val nfcManager: NfcManager,
    private val scannerPairingManager: ScannerPairingManager) : ViewModel() {

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val message: MutableLiveData<Int> = MutableLiveData(R.string.connect_scanner_bt_connect)

    val connectScannerIssue = MutableLiveData<LiveDataEventWithContent<ConnectScannerIssue>>()
    val launchAlert = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
    val scannerConnected = MutableLiveData<LiveDataEventWithContent<Boolean>>()
    val finish = MutableLiveData<LiveDataEvent>()

    val showScannerErrorDialogWithScannerId = MutableLiveData<LiveDataEventWithContent<String>>()

    private var setupFlow: Disposable? = null

    fun start() {
        startSetup()
    }

    @SuppressLint("CheckResult")
    private fun startSetup() {
        stopConnectingAndResetState()
        setupFlow = disconnectVero()
            .andThen(checkIfBluetoothIsEnabled())
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(resetVeroUI())
            .andThen(wakeUpVero())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { manageVeroErrors(it) }, onComplete = {
                handleSetupFinished()
            })
    }

    fun stopConnectingAndResetState() {
        progress.value = 0
        message.value = R.string.connect_scanner_bt_connect
        setupFlow?.dispose()
    }

    private fun disconnectVero() =
        veroTask(computeProgress(1), R.string.connect_scanner_bt_connect, "ScannerManager: disconnect",
            scannerManager.scanner { disconnect() }.onErrorComplete())

    private fun checkIfBluetoothIsEnabled() =
        veroTask(computeProgress(2), R.string.connect_scanner_bt_connect, "ScannerManager: bluetooth is enabled",
            scannerManager.checkBluetoothStatus())

    private fun initVero() =
        veroTask(computeProgress(3), R.string.connect_scanner_bt_connect, "ScannerManager: init vero",
            scannerManager.initScanner())

    private fun connectToVero() =
        veroTask(computeProgress(4), R.string.connect_scanner_bt_connect, "ScannerManager: connectToVero",
            scannerManager.scanner { connect() }) { addBluetoothConnectivityEvent() }

    private fun resetVeroUI() =
        veroTask(computeProgress(5), R.string.connect_scanner_setup, "ScannerManager: resetVeroUI",
            scannerManager.scanner { setUiIdle() })

    private fun wakeUpVero() =
        veroTask(computeProgress(6), R.string.connect_scanner_wake_un20, "ScannerManager: wakeUpVero",
            scannerManager.scanner { sensorWakeUp() }) { updateBluetoothConnectivityEventWithVeroInfo() }

    private fun updateBluetoothConnectivityEventWithVeroInfo() {
        scannerManager.let {
            sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(it.onScanner { versionInformation() }.firmwareVersion.toString())
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
            .doOnComplete {
                logMessageForCrashReport(crashReportMessage)
            }

    private fun manageVeroErrors(it: Throwable) {
        Timber.d(it)
        scannerConnected.postEvent(false)
        launchAlertOrScannerIssueOrShowDialog(scannerManager.getAlertType(it))
        if (it !is FingerprintSafeException) {
            crashReportManager.logExceptionOrSafeException(it)
        }
    }

    private fun launchAlertOrScannerIssueOrShowDialog(alert: FingerprintAlert) {
        when (alert) {
            BLUETOOTH_NOT_ENABLED ->
                connectScannerIssue.postEvent(ConnectScannerIssue.BLUETOOTH_OFF)
            NOT_PAIRED, MULTIPLE_PAIRED_SCANNERS ->
                connectScannerIssue.postEvent(determineAppropriateScannerIssueForPairing())
            DISCONNECTED ->
                scannerManager.lastPairedScannerId?.let { showScannerErrorDialogWithScannerId.postEvent(it) }
            BLUETOOTH_NOT_SUPPORTED, LOW_BATTERY, UNEXPECTED_ERROR ->
                launchAlert.postEvent(alert)
        }
    }

    private fun determineAppropriateScannerIssueForPairing(): ConnectScannerIssue {
        val couldNotBeVero1 = !preferencesManager.scannerGenerations.contains(ScannerGeneration.VERO_1)

        return if (couldNotBeVero1 && nfcManager.doesDeviceHaveNfcCapability()) {
            if (nfcManager.isNfcEnabled()) {
                ConnectScannerIssue.NFC_PAIR
            } else {
                ConnectScannerIssue.NFC_OFF
            }
        } else {
            ConnectScannerIssue.SERIAL_ENTRY_PAIR
        }
    }

    private fun handleSetupFinished() {
        progress.postValue(computeProgress(7))
        message.postValue(R.string.connect_scanner_finished)
        preferencesManager.lastScannerUsed = scannerManager.lastPairedMacAddress?.let {
            scannerPairingManager.convertAddressToSerialNumber(it)
        } ?: ""
        preferencesManager.lastScannerVersion = scannerManager.onScanner { versionInformation() }.firmwareVersion.toString()
        analyticsManager.logScannerProperties(scannerManager.lastPairedMacAddress
            ?: "", scannerManager.lastPairedScannerId ?: "")
        scannerConnected.postEvent(true)
    }

    fun retryConnect() {
        startSetup()
    }

    fun handleScannerDisconnectedYesClick() {
        connectScannerIssue.postEvent(ConnectScannerIssue.SCANNER_OFF)
    }

    fun handleScannerDisconnectedNoClick() {
        connectScannerIssue.postEvent(determineAppropriateScannerIssueForPairing())
    }

    fun handleIncorrectScanner() {
        connectScannerIssue.postEvent(determineAppropriateScannerIssueForPairing())
    }

    fun finishConnectActivity() {
        finish.postEvent()
    }

    private fun addBluetoothConnectivityEvent() {
        scannerManager.apply {
            sessionEventsManager.addEventInBackground(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerInfo(
                        lastPairedScannerId ?: "",
                        lastPairedMacAddress ?: "",
                        onScanner { versionInformation() }.firmwareVersion.toString())))
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
