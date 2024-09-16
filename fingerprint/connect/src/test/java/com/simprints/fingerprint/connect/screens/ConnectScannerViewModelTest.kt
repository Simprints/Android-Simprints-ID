package com.simprints.fingerprint.connect.screens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.fingerprint.connect.FingerprintConnectParams
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel.ConnectScannerIssueScreen
import com.simprints.fingerprint.connect.usecase.SaveScannerConnectionEventsUseCase
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.ScannerManagerImpl
import com.simprints.fingerprint.infra.scanner.ScannerPairingManager
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.infra.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerLowBatteryException
import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.infra.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerFactory
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scannermock.dummy.DummyBluetoothDevice
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.assertEventReceivedWithContent
import com.simprints.testtools.common.livedata.assertEventReceivedWithContentAssertions
import com.simprints.testtools.common.livedata.assertEventWithContentNeverReceived
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConnectScannerViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    private lateinit var fingerprintConfiguration: FingerprintConfiguration

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var bluetoothAdapter: ComponentBluetoothAdapter

    @MockK
    private lateinit var pairingManager: ScannerPairingManager

    @MockK
    private lateinit var nfcManager: NfcManager

    @MockK
    private lateinit var scannerFactory: ScannerFactory

    @MockK
    private lateinit var scannerManager: ScannerManager

    @MockK
    private lateinit var saveScannerConnectionEventsUseCase: SaveScannerConnectionEventsUseCase

    private lateinit var viewModel: ConnectScannerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_1,
            FingerprintConfiguration.VeroGeneration.VERO_2
        )
        coEvery { configManager.getProjectConfiguration().fingerprint } returns fingerprintConfiguration
        coJustRun { scannerFactory.initScannerOperationWrappers(any()) }

        scannerManager = ScannerManagerImpl(
            bluetoothAdapter,
            scannerFactory,
            pairingManager,
            SerialNumberConverter(),
            mockk(relaxed = true),
        )
        viewModel = ConnectScannerViewModel(
            configManager,
            scannerManager,
            nfcManager,
            recentUserActivityManager,
            saveScannerConnectionEventsUseCase,
        )
    }

    private fun mockScannerWrapper(
        scannerGeneration: FingerprintConfiguration.VeroGeneration,
        connectFailException: Throwable? = null
    ) = mockk<ScannerWrapper> {
        every { isConnected() } returns false
        coEvery { disconnect() } answers {}
        coEvery { connect() } answers {
            if (connectFailException != null)
                throw connectFailException
        }
        coEvery { setScannerInfoAndCheckAvailableOta(fingerprintSdk = SECUGEN_SIM_MATCHER) } answers {}
        coEvery { sensorWakeUp() } answers {}
        coEvery { setUiIdle() } answers {}
        every { versionInformation() } returns when (scannerGeneration) {
            FingerprintConfiguration.VeroGeneration.VERO_1 -> VERO_1_VERSION
            FingerprintConfiguration.VeroGeneration.VERO_2 -> VERO_2_VERSION
        }
        every { batteryInformation() } returns mockk(relaxed = true)
    }

    @Test
    fun start_bluetoothOff_sendsBluetoothOffIssueEvent() = runTest {
        setupBluetooth(isEnabled = false)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.BluetoothOff)
    }

    @Test
    fun start_bluetoothNotSupported_sendsBluetoothNotSupportedAlert() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(
            FingerprintConfiguration.VeroGeneration.VERO_2,
            connectFailException = BluetoothNotSupportedException()
        )

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.BluetoothNotSupported)
    }

    @Test
    fun startVero1_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(FingerprintConfiguration.VeroGeneration.VERO_1)
        val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
        coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk()
        coJustRun { saveScannerConnectionEventsUseCase.invoke() }

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerStepObserver = viewModel.currentStep.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerStepObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.Step.Finish.ordinal + 1) // 1 at the start

        verify(exactly = 1) { saveScannerConnectionEventsUseCase.invoke() }
        val updatedActivity = updateActivityFn.captured(RecentUserActivity("", "", "".asTokenizableRaw(), 0, 0, 0, 0))
        assertThat(updatedActivity.lastScannerUsed).isNotEmpty()
        assertThat(updatedActivity.lastScannerVersion).isEqualTo("E-1")
    }

    @Test
    fun startVero2_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(FingerprintConfiguration.VeroGeneration.VERO_2)
        val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
        coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk()

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerStepObserver = viewModel.currentStep.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerStepObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.Step.Finish.ordinal + 1) // 1 at the start

        verify(exactly = 1) { saveScannerConnectionEventsUseCase.invoke() }   // The ScannerConnectionEvent + Vero2InfoSnapshotEvent

        val updatedActivity = updateActivityFn.captured(RecentUserActivity("", "", "".asTokenizableRaw(), 0, 0, 0, 0))
        assertThat(updatedActivity.lastScannerUsed).isNotEmpty()
        assertThat(updatedActivity.lastScannerVersion).isEqualTo("E-1")
    }


    @Test
    fun start_noScannersPairedWithoutFingerprintConfigAndNfc_sendsSerialEntryIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false, isEnabled = true)
        coEvery { configManager.getProjectConfiguration().fingerprint } returns null

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.NfcPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOff_sendsNfcOffIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = false)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.NfcOff)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcNotAvailable_sendsSerialEntryIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero1WithNfcAvailableAndOn_sendsSerialEntryIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_1)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithMixedVeroGenerationsWithNfcAvailableAndOn_sendsSerialEntryIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(
            FingerprintConfiguration.VeroGeneration.VERO_1,
            FingerprintConfiguration.VeroGeneration.VERO_2
        )

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.SerialEntryPair)
    }

    @Test
    fun start_multipleScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 2)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.NfcPair)
    }

    @Test
    fun start_scannerConnectFailsWithDisconnectedException_sendsScannerConnectedFailedEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(
            FingerprintConfiguration.VeroGeneration.VERO_2,
            ScannerDisconnectedException()
        )

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
    }

    @Test
    fun start_scannerConnectFailsWithUnexpectedException_sendsAlertEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(
            FingerprintConfiguration.VeroGeneration.VERO_2,
            Error("Oops")
        )

        val scannerConnectedObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        scannerConnectedObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.UnexpectedError)
    }


    @Test
    fun start_scannerConnectFailsWithLowBatteryException_sendsAlertEvent() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(
            FingerprintConfiguration.VeroGeneration.VERO_2,
            ScannerLowBatteryException()
        )

        val scannerConnectedObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        scannerConnectedObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.LowBattery)
    }


    @Test
    fun start_scannerConnectThrowsOtaAvailableException_sendsOtaAvailableScannerIssue() = runTest {
        val e = OtaAvailableException(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.scannerWrapper } returns mockScannerWrapper(FingerprintConfiguration.VeroGeneration.VERO_2, e)

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()

        viewModel.showScannerIssueScreen.assertEventReceivedWithContentAssertions {
            assertThat(it).isInstanceOf(ConnectScannerIssueScreen.Ota::class.java)
            assertThat((it as ConnectScannerIssueScreen.Ota).availableOtas)
                .containsExactlyElementsIn(e.availableOtas)
                .inOrder()
        }
    }

    @Test
    fun handleScannerDisconnectedYesClick_sendsScannerOffEvent() = runTest {
        viewModel.handleScannerDisconnectedYesClick()

        viewModel.showScannerIssueScreen.assertEventReceivedWithContentAssertions {
            assertThat(it).isInstanceOf(ConnectScannerIssueScreen.ScannerOff::class.java)
        }
    }

    @Test
    fun handleScannerDisconnectedNoClick_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() = runTest {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()
        viewModel.handleScannerDisconnectedNoClick()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.NfcPair)
    }

    @Test
    fun handleIncorrectScanner_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() = runTest {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedScanners } returns listOf(FingerprintConfiguration.VeroGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.showScannerIssueScreen.testObserver()

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.connect()
        viewModel.handleIncorrectScanner()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssueScreen.NfcPair)
    }

    @Test
    fun finishConnectActivity_sendsFinishEvent() = runTest {
        val finishObserver = viewModel.finish.testObserver()

        viewModel.finishConnectionFlow(true)

        finishObserver.assertEventReceivedWithContent(true)
    }

    @Test
    fun startRetryingToConnect_scannerConnectFails_makesNoMoreThanMaxRetryAttempts() = runTest {
        setupBluetooth(numberOfPairedScanners = 1)
        val scannerWrapper = mockScannerWrapper(FingerprintConfiguration.VeroGeneration.VERO_1, UnknownScannerIssueException())
        every { scannerFactory.scannerWrapper } returns scannerWrapper

        viewModel.init(FingerprintConnectParams(fingerprintSDK = SECUGEN_SIM_MATCHER))
        viewModel.startRetryingToConnect()

        coVerify(exactly = 5) { scannerWrapper.connect() }
    }

    @Test
    fun handleNoBluetoothPermission_sendsAlertErrorEvent() = runTest {
        val observer = viewModel.showScannerIssueScreen.testObserver()

        viewModel.handleNoBluetoothPermission()
        observer.assertEventReceivedWithContent(ConnectScannerIssueScreen.BluetoothNoPermission)
    }

    @Test
    fun whenBackPressed_requestsExitForm() = runTest {
        val observer = viewModel.showScannerIssueScreen.testObserver()

        viewModel.handleBackPress()

        observer.assertEventReceivedWithContent(ConnectScannerIssueScreen.ExitForm)
    }

    @Test
    fun whenBackPressDisabled_doesNotRequestsExitFormOrFinish() = runTest {
        viewModel.disableBackButton()
        viewModel.handleBackPress()

        viewModel.showScannerIssueScreen.assertEventWithContentNeverReceived()
        viewModel.finish.assertEventWithContentNeverReceived()
    }


    @Test
    fun whenBackPressToExit_doesNotRequestsExitForm() = runTest {
        viewModel.setBackButtonToExitWithError()
        viewModel.handleBackPress()

        viewModel.showScannerIssueScreen.assertEventWithContentNeverReceived()
        viewModel.finish.assertEventReceivedWithContent(false)
    }


    private fun setupBluetooth(isEnabled: Boolean = true, numberOfPairedScanners: Int = 1) {
        every { bluetoothAdapter.isEnabled() } returns isEnabled
        when (numberOfPairedScanners) {
            0 -> coEvery { pairingManager.getPairedScannerAddressToUse() } throws ScannerNotPairedException()
            1 -> coEvery { pairingManager.getPairedScannerAddressToUse() } returns DummyBluetoothDevice.random().address
            else -> coEvery { pairingManager.getPairedScannerAddressToUse() } throws MultiplePossibleScannersPairedException()
        }
    }

    private fun setupNfc(doesDeviceHaveNfcCapability: Boolean? = null, isEnabled: Boolean? = null) {
        if (doesDeviceHaveNfcCapability != null) {
            every { nfcManager.doesDeviceHaveNfcCapability() } returns doesDeviceHaveNfcCapability
        }
        if (isEnabled != null) {
            every { nfcManager.isNfcEnabled() } returns isEnabled
        }
    }

    companion object {
        val VERO_1_VERSION = ScannerVersion(
            hardwareVersion = "E-1",
            generation = ScannerGeneration.VERO_1,
            firmware = ScannerFirmwareVersions(
                cypress = ScannerFirmwareVersions.UNKNOWN_VERSION,
                stm = "6.E-1.0",
                un20 = "1.E-1.0"
            ),
        )

        val VERO_2_VERSION = ScannerVersion(
            hardwareVersion = "E-1",
            generation = ScannerGeneration.VERO_2,
            ScannerFirmwareVersions(
                cypress = "1.E-1.2",
                stm = "3.E-1.4",
                un20 = "5. E-1.6"
            ),
        )
    }
}
