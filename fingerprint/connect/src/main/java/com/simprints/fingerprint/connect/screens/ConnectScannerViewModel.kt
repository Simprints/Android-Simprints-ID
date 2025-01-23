package com.simprints.fingerprint.connect.screens

import androidx.annotation.StringRes
import androidx.core.math.MathUtils.clamp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.usecase.SaveScannerConnectionEventsUseCase
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotEnabledException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.logging.LoggingConstants.AnalyticsUserProperties
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ConnectScannerViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val scannerManager: ScannerManager,
    private val nfcManager: NfcManager,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val saveScannerConnectionEvents: SaveScannerConnectionEventsUseCase,
) : ViewModel() {
    private lateinit var fingerprintSdk: FingerprintConfiguration.BioSdk
    private var allowedGenerations: List<FingerprintConfiguration.VeroGeneration> = emptyList()
    private var remainingConnectionAttempts = 0

    val currentStep: LiveData<Step>
        get() = _currentStep
    private val _currentStep = MutableLiveData(Step.Preparation)

    val isConnecting: LiveData<Boolean>
        get() = _isConnecting
    private val _isConnecting = MutableLiveData(false)

    val scannerConnected: LiveData<LiveDataEventWithContent<Boolean>>
        get() = _scannerConnected
    private val _scannerConnected = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    val showScannerIssueScreen: LiveData<LiveDataEventWithContent<ConnectScannerIssueScreen>>
        get() = _showScannerIssueScreen
    private val _showScannerIssueScreen = MutableLiveData<LiveDataEventWithContent<ConnectScannerIssueScreen>>()

    val finish: LiveData<LiveDataEventWithContent<Boolean>>
        get() = _finish
    private val _finish = MutableLiveData<LiveDataEventWithContent<Boolean>>()

    private val backButtonBehaviour = MutableLiveData(BackButtonBehaviour.EXIT_FORM)

    fun init(params: FingerprintConnectParams) = viewModelScope.launch {
        fingerprintSdk = params.fingerprintSDK
        allowedGenerations = configManager
            .getProjectConfiguration()
            .fingerprint
            ?.allowedScanners
            .orEmpty()
    }

    fun connect() {
        viewModelScope.launch { startSetup() }
    }

    fun startRetryingToConnect() {
        remainingConnectionAttempts = MAX_RETRY_COUNT - 1
        connect()
    }

    fun handleNoBluetoothPermission() {
        _showScannerIssueScreen.send(ConnectScannerIssueScreen.BluetoothNoPermission)
    }

    fun finishConnectionFlow(isSuccess: Boolean) {
        _finish.send(isSuccess)
    }

    private suspend fun startSetup() {
        _isConnecting.postValue(true)
        resetConnectionState()

        try {
            disconnectVero()
            checkIfBluetoothIsEnabled()
            initVero()
            connectToVero()
            setupVero()
            resetVeroUI()
            wakeUpVero()
            _isConnecting.postValue(false)
            setupFinished()
        } catch (ex: Throwable) {
            _isConnecting.postValue(false)
            manageVeroErrors(ex)
        }
    }

    fun resetConnectionState() {
        _currentStep.postValue(Step.Preparation)
        backButtonBehaviour.postValue(BackButtonBehaviour.EXIT_FORM)
    }

    fun handleBackPress() {
        when (backButtonBehaviour.value) {
            BackButtonBehaviour.DISABLED, null -> { // Do nothing
            }

            BackButtonBehaviour.EXIT_WITH_ERROR -> _finish.send(false)
            BackButtonBehaviour.EXIT_FORM -> {
                _scannerConnected.send(false)
                _showScannerIssueScreen.send(ConnectScannerIssueScreen.ExitForm)
            }
        }
    }

    fun disableBackButton() {
        backButtonBehaviour.postValue(BackButtonBehaviour.DISABLED)
    }

    fun setBackButtonToExitWithError() {
        backButtonBehaviour.postValue(BackButtonBehaviour.EXIT_WITH_ERROR)
    }

    private suspend fun disconnectVero() {
        if (scannerManager.isScannerConnected) {
            _currentStep.postValue(Step.DisconnectScanner)
            scannerManager.scanner.disconnect()
            logMessageForCrashReport("ScannerManager: disconnect")
        }
    }

    private suspend fun checkIfBluetoothIsEnabled() {
        _currentStep.postValue(Step.CheckBtEnabled)
        scannerManager.checkBluetoothStatus()
        logMessageForCrashReport("ScannerManager: bluetooth is enabled")
    }

    private suspend fun initVero() {
        _currentStep.postValue(Step.InitScanner)
        scannerManager.initScanner()
        logMessageForCrashReport("ScannerManager: init vero")
    }

    private suspend fun connectToVero() {
        _currentStep.postValue(Step.ConnectScanner)
        scannerManager.scanner.connect()
        logMessageForCrashReport("ScannerManager: connectToVero")
    }

    private suspend fun setupVero() {
        _currentStep.postValue(Step.SetupScanner)
        scannerManager.scanner.setScannerInfoAndCheckAvailableOta(fingerprintSdk)
        setLastConnectedScannerInfo()
        logMessageForCrashReport("ScannerManager: setupVero")
    }

    private suspend fun setLastConnectedScannerInfo() {
        recentUserActivityManager.updateRecentUserActivity {
            it.copy(
                lastScannerUsed = scannerManager.currentScannerId ?: "",
                lastScannerVersion = scannerManager.scanner.versionInformation().hardwareVersion,
            )
        }
    }

    private suspend fun resetVeroUI() {
        _currentStep.postValue(Step.ResetScannerUi)
        scannerManager.scanner.turnOffSmileLeds()
        logMessageForCrashReport("ScannerManager: resetVeroUI")
    }

    private suspend fun wakeUpVero() {
        _currentStep.postValue(Step.WakeUpScanner)
        scannerManager.scanner.sensorWakeUp()
        logMessageForCrashReport("ScannerManager: wakeUpVero")
    }

    private fun setupFinished() {
        saveScannerConnectionEvents()
        _currentStep.postValue(Step.Finish)

        Simber.setUserProperty(
            AnalyticsUserProperties.MAC_ADDRESS,
            scannerManager.currentMacAddress.orEmpty(),
        )
        Simber.setUserProperty(
            AnalyticsUserProperties.SCANNER_ID,
            scannerManager.currentScannerId.orEmpty(),
        )

        _scannerConnected.send(true)
    }

    private suspend fun manageVeroErrors(e: Throwable) {
        Simber.i("Vero connection issue", e, tag = FINGER_CAPTURE)
        _scannerConnected.send(false)

        launchAlertOrScannerIssueOrShowDialog(e)

        if (remainingConnectionAttempts > 0) {
            remainingConnectionAttempts--
            connect()
        }
    }

    private suspend fun launchAlertOrScannerIssueOrShowDialog(e: Throwable) = _showScannerIssueScreen.send(
        when (e) {
            is BluetoothNotEnabledException -> ConnectScannerIssueScreen.BluetoothOff
            is BluetoothNotSupportedException -> ConnectScannerIssueScreen.BluetoothNotSupported

            is ScannerDisconnectedException, is UnknownScannerIssueException -> ConnectScannerIssueScreen.ScannerError(
                scannerManager.currentScannerId,
            )

            is ScannerNotPairedException, is MultiplePossibleScannersPairedException -> determineAppropriateScannerIssueForPairing()
            is ScannerLowBatteryException -> ConnectScannerIssueScreen.LowBattery

            is OtaAvailableException -> {
                setLastConnectedScannerInfo()
                ConnectScannerIssueScreen.Ota(e.availableOtas)
            }

            else -> ConnectScannerIssueScreen.UnexpectedError
        },
    )

    private fun determineAppropriateScannerIssueForPairing(): ConnectScannerIssueScreen {
        val couldNotBeVero1 = !allowedGenerations.contains(FingerprintConfiguration.VeroGeneration.VERO_1)
        val hasNfc = nfcManager.doesDeviceHaveNfcCapability()

        return when {
            couldNotBeVero1 && hasNfc && nfcManager.isNfcEnabled() -> ConnectScannerIssueScreen.NfcPair
            couldNotBeVero1 && hasNfc -> ConnectScannerIssueScreen.NfcOff
            else -> ConnectScannerIssueScreen.SerialEntryPair
        }
    }

    private fun logMessageForCrashReport(message: String) {
        Simber.i(message, tag = FINGER_CAPTURE)
    }

    fun handleScannerDisconnectedYesClick() {
        _showScannerIssueScreen.send(ConnectScannerIssueScreen.ScannerOff(scannerManager.currentScannerId))
    }

    fun handleScannerDisconnectedNoClick() {
        _showScannerIssueScreen.send(determineAppropriateScannerIssueForPairing())
    }

    fun handleIncorrectScanner() {
        _showScannerIssueScreen.send(determineAppropriateScannerIssueForPairing())
    }

    private enum class BackButtonBehaviour {
        DISABLED,
        EXIT_FORM,
        EXIT_WITH_ERROR,
    }

    enum class Step(
        private val index: Int,
        @StringRes val messageRes: Int,
    ) {
        Preparation(0, R.string.fingerprint_connect_scanner_bt_connect),
        DisconnectScanner(1, R.string.fingerprint_connect_scanner_bt_connect),
        CheckBtEnabled(2, R.string.fingerprint_connect_scanner_bt_connect),
        InitScanner(3, R.string.fingerprint_connect_scanner_bt_connect),
        ConnectScanner(4, R.string.fingerprint_connect_scanner_bt_connect),
        SetupScanner(5, R.string.fingerprint_connect_scanner_setup),
        ResetScannerUi(6, R.string.fingerprint_connect_scanner_setup),
        WakeUpScanner(7, R.string.fingerprint_connect_scanner_wake_un20),
        Finish(8, R.string.fingerprint_connect_scanner_finished),
        ;

        val progress: Int
            get() = clamp(index * 100 / NUMBER_OF_STEPS, 0, 100)

        companion object {
            private const val NUMBER_OF_STEPS = 8
        }
    }

    companion object {
        private const val MAX_RETRY_COUNT = 5
    }

    sealed class ConnectScannerIssueScreen {
        data object BluetoothNoPermission : ConnectScannerIssueScreen()

        data object BluetoothOff : ConnectScannerIssueScreen()

        data object BluetoothNotSupported : ConnectScannerIssueScreen()

        data object NfcPair : ConnectScannerIssueScreen()

        data object NfcOff : ConnectScannerIssueScreen()

        data object SerialEntryPair : ConnectScannerIssueScreen()

        data object LowBattery : ConnectScannerIssueScreen()

        data class ScannerOff(
            val currentScannerId: String?,
        ) : ConnectScannerIssueScreen()

        data class ScannerError(
            val currentScannerId: String?,
        ) : ConnectScannerIssueScreen()

        data class Ota(
            val availableOtas: List<AvailableOta>,
        ) : ConnectScannerIssueScreen()

        data object UnexpectedError : ConnectScannerIssueScreen()

        data object ExitForm : ConnectScannerIssueScreen()
    }
}
