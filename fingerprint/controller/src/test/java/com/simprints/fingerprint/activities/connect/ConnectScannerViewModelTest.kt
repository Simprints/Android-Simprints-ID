package com.simprints.fingerprint.activities.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel.Companion.MAX_RETRY_COUNT
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.ScannerConnectionEvent
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.exceptions.safe.MultiplePossibleScannersPairedException
import com.simprints.fingerprint.scanner.exceptions.safe.OtaAvailableException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerNotPairedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.UnknownScannerIssueException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprint.testtools.assertEventReceivedWithContentAssertions
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.infra.config.domain.models.FingerprintConfiguration.VeroGeneration.VERO_1
import com.simprints.infra.config.domain.models.FingerprintConfiguration.VeroGeneration.VERO_2
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
    private lateinit var sessionEventsManager: FingerprintSessionEventsManager

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

    private lateinit var viewModel: ConnectScannerViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_1, VERO_2)
        coEvery { configManager.getProjectConfiguration().fingerprint } returns fingerprintConfiguration

        scannerManager = ScannerManagerImpl(bluetoothAdapter, scannerFactory, pairingManager, SerialNumberConverter())
        viewModel = ConnectScannerViewModel(
            scannerManager,
            mockk(relaxed = true),
            sessionEventsManager,
            recentUserActivityManager,
            configManager,
            nfcManager,
        )
    }


    private fun mockScannerWrapper(
        scannerGeneration: FingerprintConfiguration.VeroGeneration,
        connectFailException: Throwable? = null
    ) =
        mockk<ScannerWrapper> {
            coEvery { disconnect() } answers {}
            coEvery { connect() } answers {
                if (connectFailException != null)
                    throw connectFailException
            }
            coEvery { setScannerInfoAndCheckAvailableOta() } answers {}
            coEvery { sensorWakeUp() } answers {}
            coEvery { setUiIdle() } answers {}
            every { versionInformation() } returns when (scannerGeneration) {
                VERO_1 -> VERO_1_VERSION
                VERO_2 -> VERO_2_VERSION
            }
            every { batteryInformation() } returns mockk(relaxed = true)
        }

    @Test
    fun start_bluetoothOff_sendsBluetoothOffIssueEvent() {
        setupBluetooth(isEnabled = false)
        coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.BluetoothOff)
    }

    @Test
    fun start_bluetoothNotSupported_sendsBluetoothNotSupportedAlert() {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(
            VERO_2,
            connectFailException = BluetoothNotSupportedException()
        )

        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        launchAlertObserver.assertEventReceivedWithContent(AlertError.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun startVero1_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() =
        runTest {
            setupBluetooth(numberOfPairedScanners = 1)
            coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_1)
            val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk()
            var scannerConnectionEvent: ScannerConnectionEvent? = null
            every { sessionEventsManager.addEventInBackground(any()) } answers {
                scannerConnectionEvent = args[0] as ScannerConnectionEvent
            }
            val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
            val scannerProgressObserver = viewModel.progress.testObserver()

            viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
            viewModel.start()

            assertThat(scannerConnectionEvent?.scannerInfo?.hardwareVersion).isEqualTo(
                VERO_1_VERSION.firmware.stm
            )
            scannerConnectedObserver.assertEventReceivedWithContent(true)
            assertThat(scannerProgressObserver.observedValues.size).isEqualTo(
                ConnectScannerViewModel.NUMBER_OF_STEPS + 1
            ) // 1 at the start
            verify(exactly = 1) { sessionEventsManager.addEventInBackground(any()) }
            verify(exactly = 1) { sessionEventsManager.addEventInBackground(any()) }
            val updatedActivity =
                updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(updatedActivity.lastScannerUsed).isNotEmpty()
            assertThat(updatedActivity.lastScannerVersion).isEqualTo("E-1")
        }

    @Test
    fun startVero2_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() =
        runTest {
            setupBluetooth(numberOfPairedScanners = 1)
            coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2)
            val updateActivityFn = slot<suspend (RecentUserActivity) -> RecentUserActivity>()
            coEvery { recentUserActivityManager.updateRecentUserActivity(capture(updateActivityFn)) } returns mockk()
            var scannerConnectionEvent: ScannerConnectionEvent? = null
            every { sessionEventsManager.addEventInBackground(any()) } answers {
                if (args[0] is ScannerConnectionEvent) {
                    scannerConnectionEvent = args[0] as ScannerConnectionEvent
                }
            }
            val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
            val scannerProgressObserver = viewModel.progress.testObserver()

            viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
            viewModel.start()

            scannerConnectedObserver.assertEventReceivedWithContent(true)
            assertThat(scannerProgressObserver.observedValues.size).isEqualTo(
                ConnectScannerViewModel.NUMBER_OF_STEPS + 1
            ) // 1 at the start
            verify(exactly = 2) { sessionEventsManager.addEventInBackground(any()) }    // The ScannerConnectionEvent + Vero2InfoSnapshotEvent
            assertThat(scannerConnectionEvent?.scannerInfo?.hardwareVersion).isEqualTo(VERO_2_VERSION.hardwareVersion)
            val updatedActivity =
                updateActivityFn.captured(RecentUserActivity("", "", "", 0, 0, 0, 0))
            assertThat(updatedActivity.lastScannerUsed).isNotEmpty()
            assertThat(updatedActivity.lastScannerVersion).isEqualTo("E-1")
        }


    @Test
    fun start_noScannersPairedWithoutFingerprintConfigAndNfc_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false, isEnabled = true)
        coEvery { configManager.getProjectConfiguration().fingerprint } returns null

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOff_sendsNfcOffIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = false)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcOff)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcNotAvailable_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero1WithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_1)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithMixedVeroGenerationsWithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_1, VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_multipleScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 2)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_scannerConnectFailsWithDisconnectedException_sendsScannerConnectedFailedEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(
            VERO_2,
            ScannerDisconnectedException()
        )

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
    }

    @Test
    fun start_scannerConnectFailsWithUnexpectedException_sendsAlertEvent() {
        val error = Error("Oops")
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2, error)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
        launchAlertObserver.assertEventReceivedWithContent(AlertError.UNEXPECTED_ERROR)
    }

    @Test
    fun start_scannerConnectThrowsOtaAvailableException_sendsOtaAvailableScannerIssue() {
        val e = OtaAvailableException(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        setupBluetooth(numberOfPairedScanners = 1)
        coEvery { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2, e)

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        viewModel.connectScannerIssue.assertEventReceivedWithContentAssertions {
            assertThat(it).isInstanceOf(ConnectScannerIssue.Ota::class.java)
            assertThat((it as ConnectScannerIssue.Ota).otaFragmentRequest.availableOtas).containsExactlyElementsIn(
                e.availableOtas
            )
                .inOrder()
        }
    }

    @Test
    fun handleScannerDisconnectedYesClick_sendsScannerOffEvent() {
        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.handleScannerDisconnectedYesClick()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.ScannerOff)
    }

    @Test
    fun handleScannerDisconnectedNoClick_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.handleScannerDisconnectedNoClick()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun handleIncorrectScanner_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { fingerprintConfiguration.allowedVeroGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.handleIncorrectScanner()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun finishConnectActivity_sendsFinishEvent() {
        val finishObserver = viewModel.finish.testObserver()

        viewModel.finishConnectActivity()

        finishObserver.assertEventReceived()
    }

    @Test
    fun startRetryingToConnect_scannerConnectFails_makesNoMoreThanMaxRetryAttempts() {
        setupBluetooth(numberOfPairedScanners = 1)
        val scannerWrapper = mockScannerWrapper(VERO_1, UnknownScannerIssueException())
        coEvery { scannerFactory.create(any()) } returns scannerWrapper

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.startRetryingToConnect()

        coVerify(exactly = MAX_RETRY_COUNT) { scannerWrapper.connect() }
    }

    private fun setupBluetooth(isEnabled: Boolean = true, numberOfPairedScanners: Int = 1) {
        every { bluetoothAdapter.isEnabled() } returns isEnabled
        when (numberOfPairedScanners) {
            0 -> coEvery { pairingManager.getPairedScannerAddressToUse() } throws ScannerNotPairedException()
            1 -> coEvery { pairingManager.getPairedScannerAddressToUse() } returns com.simprints.fingerprint.scannermock.dummy.DummyBluetoothDevice.random().address
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
