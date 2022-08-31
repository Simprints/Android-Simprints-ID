package com.simprints.fingerprint.activities.connect

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.tools.coroutines.DispatcherProvider
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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ConnectScannerViewModel(
    private val scannerManager: ScannerManager,
    private val timeHelper: FingerprintTimeHelper,
    private val sessionEventsManager: FingerprintSessionEventsManager,
    private val preferencesManager: FingerprintPreferencesManager,
    private val nfcManager: NfcManager,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    lateinit var connectMode: ConnectScannerTaskRequest.ConnectMode

    val progress: MutableLiveData<Int> = MutableLiveData(0)
    val message: MutableLiveData<Int> = MutableLiveData(R.string.connect_scanner_bt_connect)
    private val _isConnecting = MutableLiveData(false)
    val isConnecting: LiveData<Boolean> = _isConnecting
    val backButtonBehaviour: MutableLiveData<BackButtonBehaviour> =
        MutableLiveData(BackButtonBehaviour.EXIT_FORM)

    val connectScannerIssue = MutableLiveData<LiveDataEventWithContent<ConnectScannerIssue>>()
    val launchAlert = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
    val scannerConnected = MutableLiveData<LiveDataEventWithContent<Boolean>>()
    val finish = MutableLiveData<LiveDataEvent>()
    val finishAfterError = MutableLiveData<LiveDataEvent>()

    val showScannerErrorDialogWithScannerId = MutableLiveData<LiveDataEventWithContent<String>>()

    private var remainingConnectionAttempts = 0

    fun init(connectMode: ConnectScannerTaskRequest.ConnectMode) {
        this.connectMode = connectMode
    }

    fun start(){
        runBlocking { startSetup() }
     }fun retryConnect() {
        startSetup()
    }

    fun startRetryingToConnect() {
        remainingConnectionAttempts = MAX_RETRY_COUNT - 1
        retryConnect()
    }
    @SuppressLint("CheckResult")
    private suspend fun startSetup() {
        _isConnecting.postValue(true)
        stopConnectingAndResetState()

        viewModelScope.launch(dispatcherProvider.io()) {
            try {
                disconnectVero()
                checkIfBluetoothIsEnabled()
                initVero()
                connectToVero()
                setupVero()
                resetVeroUI()
                wakeUpVero()
                _isConnecting.postValue(false)
                handleSetupFinished()
            } catch (ex : Throwable) {
                _isConnecting.postValue(false)
                manageVeroErrors(ex)
            }
        }
    }

    fun stopConnectingAndResetState() {
        progress.value = 0
        message.value = R.string.connect_scanner_bt_connect
        backButtonBehaviour.value = BackButtonBehaviour.EXIT_FORM
    }

    private suspend fun disconnectVero() {
        if (scannerManager.isScannerAvailable) {
            postProgressAndMessage(step = 1, messageRes = R.string.connect_scanner_bt_connect)
            scannerManager.scanner.disconnect()
            logMessageForCrashReport("ScannerManager: disconnect")
        }
    }

    private suspend fun checkIfBluetoothIsEnabled() {
        postProgressAndMessage(step = 2, messageRes = R.string.connect_scanner_bt_connect)
        scannerManager.checkBluetoothStatus()
        logMessageForCrashReport("ScannerManager: bluetooth is enabled")
    }

    private suspend fun initVero() {
        postProgressAndMessage(step = 3, messageRes = R.string.connect_scanner_bt_connect)
        scannerManager.initScanner()
        logMessageForCrashReport("ScannerManager: init vero")
    }

    private suspend fun connectToVero() {
        postProgressAndMessage(
            step = 4,
            messageRes = R.string.connect_scanner_bt_connect
        )

        scannerManager.scanner.connect()

        addBluetoothConnectivityEvent()
        logMessageForCrashReport("ScannerManager: connectToVero")
    }

    private suspend fun setupVero() {
        postProgressAndMessage(step = 5, messageRes = R.string.connect_scanner_setup)
        scannerManager.scanner.setScannerInfoAndCheckAvailableOta()
        setLastConnectedScannerInfo()
        addInfoSnapshotEventIfNecessary()
        logMessageForCrashReport("ScannerManager: setupVero")
    }

    private suspend fun resetVeroUI() {
        postProgressAndMessage(step = 6, messageRes = R.string.connect_scanner_setup)
        scannerManager.scanner.setUiIdle()
        logMessageForCrashReport("ScannerManager: resetVeroUI")
    }

    private suspend fun wakeUpVero() {
        postProgressAndMessage(step = 7, messageRes = R.string.connect_scanner_wake_un20)
        scannerManager.scanner.sensorWakeUp()
        updateBluetoothConnectivityEventWithVeroInfoIfNecessary()
        logMessageForCrashReport("ScannerManager: wakeUpVero")
    }

    private fun updateBluetoothConnectivityEventWithVeroInfoIfNecessary() {
        if (!scannerManager.isScannerAvailable)
            return retryConnect()


        with(scannerManager.scanner) {
            if (versionInformation().generation == ScannerGeneration.VERO_1) {
                sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(
                    versionInformation().firmware.stm
                )
            }
        }
    }

    private fun postProgressAndMessage(step: Int,  @StringRes messageRes: Int) {
        val progress = computeProgress(step)
        this.progress.postValue(progress)
        this.message.postValue(messageRes)
    }


    private fun manageVeroErrors(e: Throwable) {
        Simber.d(e)
        scannerConnected.postEvent(false)
        launchAlertOrScannerIssueOrShowDialog(e)
        if (e !is FingerprintSafeException) {
            Simber.e(e)
        }
        if (remainingConnectionAttempts > 0) {
            remainingConnectionAttempts--
            retryConnect()
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
                    showScannerErrorDialogWithScannerId.postEvent(it)
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
            scannerManager.scanner.versionInformation().hardwareVersion
    }

    private fun handleSetupFinished() {
        progress.postValue(computeProgress(7))
        message.postValue(R.string.connect_scanner_finished)

        Simber.tag(MAC_ADDRESS, true).i(scannerManager.currentMacAddress ?: "")
        Simber.tag(SCANNER_ID, true).i(scannerManager.currentScannerId ?: "")

        scannerConnected.postEvent(true)
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
        if (!scannerManager.isScannerAvailable)
            return retryConnect()


        with(scannerManager.scanner) {
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
        }
    }

    private fun addInfoSnapshotEventIfNecessary() {
        if (!scannerManager.isScannerAvailable)
            return retryConnect()


        with(scannerManager.scanner) {
            if (versionInformation().generation == ScannerGeneration.VERO_2) {
                sessionEventsManager.addEventInBackground(
                    Vero2InfoSnapshotEvent(
                        timeHelper.now(),
                        Vero2InfoSnapshotEvent.Vero2Version.get(versionInformation()),
                        Vero2InfoSnapshotEvent.BatteryInfo.get(batteryInformation())
                    )
                )
            }
        }
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.tag(CrashReportTag.SCANNER_SETUP.name).i(message)
    }

    fun logScannerErrorDialogShownToCrashReport() {
        Simber.tag(CrashReportTag.ALERT.name).i("Scanner error confirm dialog shown")
    }


    enum class BackButtonBehaviour {
        DISABLED,
        EXIT_FORM,
        EXIT_WITH_ERROR
    }

    companion object {
        const val NUMBER_OF_STEPS = 8
        const val MAX_RETRY_COUNT = 5
        private fun computeProgress(step: Int) = step * 100 / NUMBER_OF_STEPS
    }
}
