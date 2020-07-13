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
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTag
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportTrigger
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.core.eventData.model.Vero2InfoSnapshotEvent
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.exceptions.safe.FingerprintSafeException
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
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
    private val nfcManager: NfcManager) : ViewModel() {

    lateinit var connectMode: ConnectScannerTaskRequest.ConnectMode

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val message: MutableLiveData<Int> = MutableLiveData(R.string.connect_scanner_bt_connect)
    val backButtonBehaviour: MutableLiveData<BackButtonBehaviour> = MutableLiveData(BackButtonBehaviour.EXIT_FORM)

    val connectScannerIssue = MutableLiveData<LiveDataEventWithContent<ConnectScannerIssue>>()
    val launchAlert = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
    val scannerConnected = MutableLiveData<LiveDataEventWithContent<Boolean>>()
    val finish = MutableLiveData<LiveDataEvent>()
    val finishAfterError = MutableLiveData<LiveDataEvent>()

    val showScannerErrorDialogWithScannerId = MutableLiveData<LiveDataEventWithContent<String>>()

    private var setupFlow: Disposable? = null

    fun start(connectMode: ConnectScannerTaskRequest.ConnectMode) {
        this.connectMode = connectMode
        startSetup()
    }

    @SuppressLint("CheckResult")
    private fun startSetup() {
        stopConnectingAndResetState()
        setupFlow = disconnectVero()
            .andThen(checkIfBluetoothIsEnabled())
            .andThen(initVero())
            .andThen(connectToVero())
            .andThen(setupVero())
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
        backButtonBehaviour.value = BackButtonBehaviour.EXIT_FORM
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

    private fun setupVero() =
        veroTask(computeProgress(5), R.string.connect_scanner_setup, "ScannerManager: setupVero",
            scannerManager.scanner { setup() }) { addInfoSnapshotEventIfNecessary() }

    private fun resetVeroUI() =
        veroTask(computeProgress(6), R.string.connect_scanner_setup, "ScannerManager: resetVeroUI",
            scannerManager.scanner { setUiIdle() })

    private fun wakeUpVero() =
        veroTask(computeProgress(7), R.string.connect_scanner_wake_un20, "ScannerManager: wakeUpVero",
            scannerManager.scanner { sensorWakeUp() }) { updateBluetoothConnectivityEventWithVeroInfoIfNecessary() }

    private fun updateBluetoothConnectivityEventWithVeroInfoIfNecessary() {
        scannerManager.let {
            if (scannerManager.onScanner { versionInformation() }.generation == ScannerGeneration.VERO_1) {
                sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(it.onScanner { versionInformation() }.computeMasterVersion().toString())
            }
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

    private fun manageVeroErrors(e: Throwable) {
        Timber.d(e)
        scannerConnected.postEvent(false)
        launchAlertOrScannerIssueOrShowDialog(e)
        if (e !is FingerprintSafeException) {
            crashReportManager.logExceptionOrSafeException(e)
        }
    }

    private fun launchAlertOrScannerIssueOrShowDialog(e: Throwable) {
        when (e) {
            is BluetoothNotEnabledException ->
                connectScannerIssue.postEvent(ConnectScannerIssue.BluetoothOff)
            is ScannerNotPairedException, is MultipleScannersPairedException ->
                connectScannerIssue.postEvent(determineAppropriateScannerIssueForPairing())
            is ScannerDisconnectedException, is UnknownScannerIssueException ->
                scannerManager.currentScannerId?.let { showScannerErrorDialogWithScannerId.postEvent(it) }
            is OtaAvailableException ->
                connectScannerIssue.postEvent(ConnectScannerIssue.Ota(OtaFragmentRequest(e.availableOtas)))
            is BluetoothNotSupportedException ->
                launchAlert.postEvent(BLUETOOTH_NOT_SUPPORTED)
            is ScannerLowBatteryException ->
                launchAlert.postEvent(LOW_BATTERY)
            else ->
                launchAlert.postEvent(UNEXPECTED_ERROR)
        }
    }

    private fun determineAppropriateScannerIssueForPairing(): ConnectScannerIssue {
        val couldNotBeVero1 = !preferencesManager.scannerGenerations.contains(ScannerGeneration.VERO_1)

        return if (couldNotBeVero1 && nfcManager.doesDeviceHaveNfcCapability()) {
            if (nfcManager.isNfcEnabled()) {
                ConnectScannerIssue.NfcPair
            } else {
                ConnectScannerIssue.NfcOff
            }
        } else {
            ConnectScannerIssue.SerialEntryPair
        }
    }

    private fun handleSetupFinished() {
        progress.postValue(computeProgress(7))
        message.postValue(R.string.connect_scanner_finished)
        preferencesManager.lastScannerUsed = scannerManager.currentScannerId ?: ""
        preferencesManager.lastScannerVersion = scannerManager.onScanner { versionInformation() }.computeMasterVersion().toString()
        analyticsManager.logScannerProperties(scannerManager.currentMacAddress
            ?: "", scannerManager.currentScannerId ?: "")
        scannerConnected.postEvent(true)
    }

    fun retryConnect() {
        startSetup()
    }

    fun handleScannerDisconnectedYesClick() {
        connectScannerIssue.postEvent(ConnectScannerIssue.ScannerOff)
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

    fun disableBackButton() {
        backButtonBehaviour.value = BackButtonBehaviour.DISABLED
    }

    fun setBackButtonToExitWithError() {
        backButtonBehaviour.value = BackButtonBehaviour.EXIT_WITH_ERROR
    }

    private fun addBluetoothConnectivityEvent() {
        with(scannerManager) {
            sessionEventsManager.addEventInBackground(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerInfo(
                        currentScannerId ?: "",
                        currentMacAddress ?: "",
                        ScannerConnectionEvent.ScannerGeneration.get(onScanner { versionInformation().generation }),
                        null)))
        }
    }

    private fun addInfoSnapshotEventIfNecessary() {
        with(scannerManager) {
            if (onScanner { versionInformation().generation } == ScannerGeneration.VERO_2) {
                sessionEventsManager.addEventInBackground(
                    Vero2InfoSnapshotEvent(
                        timeHelper.now(),
                        Vero2InfoSnapshotEvent.Vero2Version.get(onScanner { versionInformation() }),
                        Vero2InfoSnapshotEvent.BatteryInfo.get(onScanner { batteryInformation() })
                    )
                )
            }
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

    enum class BackButtonBehaviour {
        DISABLED,
        EXIT_FORM,
        EXIT_WITH_ERROR
    }

    companion object {
        const val NUMBER_OF_STEPS = 8
        private fun computeProgress(step: Int) = step * 100 / NUMBER_OF_STEPS
    }
}
