package com.simprints.fingerprint.activities.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.domain.ScannerGeneration.VERO_1
import com.simprints.fingerprint.scanner.domain.ScannerGeneration.VERO_2
import com.simprints.fingerprint.scanner.domain.ota.AvailableOta
import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.exceptions.safe.*
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.pairing.ScannerPairingManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprint.testtools.assertEventReceivedWithContentAssertions
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothDevice
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class ConnectScannerViewModelTest : KoinTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val preferencesManager: FingerprintPreferencesManager = mockk(relaxed = true)
    private val bluetoothAdapter: ComponentBluetoothAdapter = mockk()
    private val pairingManager: ScannerPairingManager = mockk()
    private val nfcManager: NfcManager = mockk()
    private val scannerFactory: ScannerFactory = mockk()

    private lateinit var viewModel: ConnectScannerViewModel

    private val mockDispatcher = mockk<DispatcherProvider> {
        every { main() } returns testCoroutineRule.testCoroutineDispatcher
        every { default() } returns testCoroutineRule.testCoroutineDispatcher
        every { io() } returns testCoroutineRule.testCoroutineDispatcher
    }

    @Before
    fun setUp() {
        val mockModule = module(override = true) {
            factory { mockk<FingerprintTimeHelper>(relaxed = true) }
            factory { sessionEventsManager }
            factory { preferencesManager }
            factory { bluetoothAdapter }
            factory { pairingManager }
            factory { nfcManager }
            factory { scannerFactory }
            factory { mockDispatcher }
        }
        loadKoinModules(mockModule)

        viewModel = get()
    }

    private fun mockScannerWrapper(scannerGeneration: ScannerGeneration, connectFailException: Throwable? = null) =
        mockk<ScannerWrapper> {
            coEvery { disconnect() } answers {}
            coEvery { connect() } answers {
                if (connectFailException != null)
                    throw connectFailException
            }
            coEvery { setup() } answers {}
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
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.BluetoothOff)
    }

    @Test
    fun start_bluetoothNotSupported_sendsBluetoothNotSupportedAlert() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(
            VERO_2,
            connectFailException = BluetoothNotSupportedException()
        )

        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun startVero1_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_1)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerProgressObserver = viewModel.progress.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerProgressObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.NUMBER_OF_STEPS + 1) // 1 at the start
        verify { preferencesManager.lastScannerUsed = any() }
        verify { preferencesManager.lastScannerVersion = any() }
        verify(exactly = 1) { sessionEventsManager.addEventInBackground(any()) }
        verify(exactly = 1) { sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(any()) }
    }

    @Test
    fun startVero2_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerProgressObserver = viewModel.progress.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerProgressObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.NUMBER_OF_STEPS + 1) // 1 at the start
        verify { preferencesManager.lastScannerUsed = any() }
        verify { preferencesManager.lastScannerVersion = any() }
        verify(exactly = 2) { sessionEventsManager.addEventInBackground(any()) }    // The ScannerConnectionEvent + Vero2InfoSnapshotEvent
        verify(exactly = 0) { sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(any()) }
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOff_sendsNfcOffIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = false)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcOff)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcNotAvailable_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero1WithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_1)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithMixedVeroGenerationsWithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_1, VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_multipleScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 2)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_scannerConnectFailsWithDisconnectedException_sendsScannerConnectedFailedEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2, ScannerDisconnectedException())

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
    }

    @Test
    fun start_scannerConnectFailsWithUnexpectedException_sendsAlertEvent() {
        val error = Error("Oops")
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2, error)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.UNEXPECTED_ERROR)
    }

    @Test
    fun start_scannerConnectThrowsOtaAvailableException_sendsOtaAvailableScannerIssue() {
        val e = OtaAvailableException(listOf(AvailableOta.CYPRESS, AvailableOta.UN20))
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(VERO_2, e)

        viewModel.init(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)
        viewModel.start()

        viewModel.connectScannerIssue.assertEventReceivedWithContentAssertions {
            assertThat(it).isInstanceOf(ConnectScannerIssue.Ota::class.java)
            assertThat((it as ConnectScannerIssue.Ota).otaFragmentRequest.availableOtas).containsExactlyElementsIn(e.availableOtas)
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
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.handleScannerDisconnectedNoClick()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun handleIncorrectScanner_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.handleIncorrectScanner()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun finishConnectActivity_sendsFinishEvent() {
        val finishObserver = viewModel.finish.testObserver()

        viewModel.finishConnectActivity()

        finishObserver.assertEventReceived()
    }

    private fun setupBluetooth(isEnabled: Boolean = true, numberOfPairedScanners: Int = 1) {
        every { bluetoothAdapter.isEnabled() } returns isEnabled
        when (numberOfPairedScanners) {
            0 -> every { pairingManager.getPairedScannerAddressToUse() } throws ScannerNotPairedException()
            1 -> every { pairingManager.getPairedScannerAddressToUse() } returns DummyBluetoothDevice.random().address
            else -> every { pairingManager.getPairedScannerAddressToUse() } throws MultiplePossibleScannersPairedException()
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
            generation = VERO_1,
            firmware = ScannerFirmwareVersions(
                cypress = ScannerFirmwareVersions.UNKNOWN_VERSION,
                stm = "6.E-1.0",
                un20 = "1.E-1.0"
            ),
        )

        val VERO_2_VERSION = ScannerVersion(
            hardwareVersion = "E-1",
            generation = VERO_2,
            ScannerFirmwareVersions(
                cypress = "1.E-1.2",
                stm = "3.E-1.4",
                un20 = "5. E-1.6"
            ),
        )
    }
}
