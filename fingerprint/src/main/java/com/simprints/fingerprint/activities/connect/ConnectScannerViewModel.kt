package com.simprints.fingerprint.activities.connect

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.simprints.core.analytics.CrashReportTag
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert.*
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.issues.ota.OtaFragmentRequest
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
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
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties.MAC_ADDRESS
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties.SCANNER_ID
import com.simprints.infra.logging.Simber
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class ConnectScannerViewModel(
    private val scannerManager: ScannerManager,
    private val timeHelper: FingerprintTimeHelper,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val preferencesManager: FingerprintPreferencesManager,
    private val nfcManager: NfcManager
) : ViewModel() {

    lateinit var connectMode: ConnectScannerTaskRequest.ConnectMode

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val message: MutableLiveData<Int> = MutableLiveData(R.string.connect_scanner_bt_connect)
    val backButtonBehaviour: MutableLiveData<BackButtonBehaviour> =
        MutableLiveData(BackButtonBehaviour.EXIT_FORM)

    val connectScannerIssue = MutableLiveData<LiveDataEventWithContent<ConnectScannerIssue>>()
    val launchAlert = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
    val scannerConnected = MutableLiveData<LiveDataEventWithContent<Boolean>>()
    val finish = MutableLiveData<LiveDataEvent>()
    val finishAfterError = MutableLiveData<LiveDataEvent>()

    val showScannerErrorDialogWithScannerId = MutableLiveData<LiveDataEventWithContent<String>>()

    private var setupFlow: Disposable? = null

    fun init(connectMode: ConnectScannerTaskRequest.ConnectMode) {
        this.connectMode = connectMode
    }

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
            .andThen(setupVero())
            .andThen(resetVeroUI())
            .andThen(wakeUpVero())
            .subscribeOn(Schedulers.io())
            .subscribeBy(onError = { manageVeroErrors(it) }, onComplete = {
                handleSetupFinished()
            })
    }

    fun stopConnectingAndResetState() {
        progress.postValue(0)
        message.postValue(R.string.connect_scanner_bt_connect)
        backButtonBehaviour.postValue(BackButtonBehaviour.EXIT_FORM)
        setupFlow?.dispose()
    }

    private fun disconnectVero() =
        veroTask(computeProgress(1),
            R.string.connect_scanner_bt_connect,
            "ScannerManager: disconnect",
            scannerManager.scanner { disconnect() }.onErrorComplete()
        )

    private fun checkIfBluetoothIsEnabled() =
        veroTask(
            computeProgress(2),
            R.string.connect_scanner_bt_connect,
            "ScannerManager: bluetooth is enabled",
            scannerManager.checkBluetoothStatus()
        )

    private fun initVero() =
        veroTask(
            computeProgress(3), R.string.connect_scanner_bt_connect, "ScannerManager: init vero",
            scannerManager.initScanner()
        )

    private fun connectToVero() = veroTask(computeProgress(4),
        R.string.connect_scanner_bt_connect,
        "ScannerManager: connectToVero",
        scannerManager.scanner { connect() }) {
            addBluetoothConnectivityEvent()
        }

    private fun setupVero() = veroTask(
        computeProgress(5),
        R.string.connect_scanner_setup,
        "ScannerManager: setupVero",
        scannerManager.scanner { setup() }) {
            setLastConnectedScannerInfo()
            addInfoSnapshotEventIfNecessary()
        }

    private fun resetVeroUI() =
        veroTask(computeProgress(6), R.string.connect_scanner_setup, "ScannerManager: resetVeroUI",
            scannerManager.scanner { setUiIdle() })

    private fun wakeUpVero() =
        veroTask(computeProgress(7),
            R.string.connect_scanner_wake_un20,
            "ScannerManager: wakeUpVero",
            scannerManager.scanner { sensorWakeUp() }) { updateBluetoothConnectivityEventWithVeroInfoIfNecessary() }

    private fun updateBluetoothConnectivityEventWithVeroInfoIfNecessary() {
        scannerManager.scanner?.run {
            if (versionInformation().generation == ScannerGeneration.VERO_1) {
                sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(
                    versionInformation().firmware.stm
                )
            }
        } ?: retryConnect()
    }

    private fun veroTask(
        progress: Int, @StringRes messageRes: Int, crashReportMessage: String,
        task: Completable, callback: (() -> Unit)? = null
    ): Completable =
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
        Simber.d(e)
        scannerConnected.postEvent(false)
        launchAlertOrScannerIssueOrShowDialog(e)
        if (e !is FingerprintSafeException) {
            Simber.e(e)
        }
    }

    private fun launchAlertOrScannerIssueOrShowDialog(e: Throwable) {
        when (e) {
            is BluetoothNotEnabledException ->
                connectScannerIssue.postEvent(ConnectScannerIssue.BluetoothOff)
            is ScannerNotPairedException, is MultiplePossibleScannersPairedException ->
                connectScannerIssue.postEvent(determineAppropriateScannerIssueForPairing())
            is ScannerDisconnectedException, is UnknownScannerIssueException ->
                scannerManager.currentScannerId?.let {
                    showScannerErrorDialogWithScannerId.postEvent(
                        it
                    )
                }
            is OtaAvailableException -> {
                setLastConnectedScannerInfo()
                connectScannerIssue.postEvent(ConnectScannerIssue.Ota(OtaFragmentRequest(e.availableOtas)))
            }
            is BluetoothNotSupportedException ->
                launchAlert.postEvent(BLUETOOTH_NOT_SUPPORTED)
            is ScannerLowBatteryException ->
                launchAlert.postEvent(LOW_BATTERY)
            else ->
                launchAlert.postEvent(UNEXPECTED_ERROR)
        }
    }

    private fun determineAppropriateScannerIssueForPairing(): ConnectScannerIssue {
        val couldNotBeVero1 =
            !preferencesManager.scannerGenerations.contains(ScannerGeneration.VERO_1)

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

    private fun setLastConnectedScannerInfo() {
        preferencesManager.lastScannerUsed = scannerManager.currentScannerId ?: ""
        preferencesManager.lastScannerVersion =
            scannerManager.scanner?.versionInformation()?.hardwareVersion ?: ""
    }

    private fun handleSetupFinished() {
        progress.postValue(computeProgress(7))
        message.postValue(R.string.connect_scanner_finished)

        Simber.tag(MAC_ADDRESS, true).i(scannerManager.currentMacAddress ?: "")
        Simber.tag(SCANNER_ID, true).i(scannerManager.currentScannerId ?: "")

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
        backButtonBehaviour.postValue(BackButtonBehaviour.DISABLED)
    }

    fun setBackButtonToExitWithError() {
        backButtonBehaviour.postValue(BackButtonBehaviour.EXIT_WITH_ERROR)
    }

    private fun addBluetoothConnectivityEvent() {
        scannerManager.scanner?.run {
            sessionEventsManager.addEventInBackground(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerInfo(
                        scannerManager.currentScannerId ?: "",
                        scannerManager.currentMacAddress ?: "",
                        ScannerConnectionEvent.ScannerGeneration.get(versionInformation().generation),
                        null
                    )
                )
            )
        } ?: retryConnect()
    }

    private fun addInfoSnapshotEventIfNecessary() {
        scannerManager.scanner?.run {
            if (versionInformation().generation == ScannerGeneration.VERO_2) {
                sessionEventsManager.addEventInBackground(
                    Vero2InfoSnapshotEvent(
                        timeHelper.now(),
                        Vero2InfoSnapshotEvent.Vero2Version.get(versionInformation()),
                        Vero2InfoSnapshotEvent.BatteryInfo.get(batteryInformation())
                    )
                )
            }
        } ?: retryConnect()
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.SCANNER_SETUP.name).i(message)
    }

    fun logScannerErrorDialogShownToCrashReport() {
        Simber.tag(CrashReportTag.ALERT.name).i("Scanner error confirm dialog shown")
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
