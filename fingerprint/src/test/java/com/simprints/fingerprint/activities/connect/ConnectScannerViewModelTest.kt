package com.simprints.fingerprint.activities.connect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.safe.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
import com.simprints.fingerprint.testtools.assertEventReceived
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscannermock.dummy.DummyBluetoothDevice
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declareModule

class ConnectScannerViewModelTest : KoinTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val fingerprintAnalyticsManager: FingerprintAnalyticsManager = mockk(relaxed = true)
    private val crashReportManager: FingerprintCrashReportManager = mockk(relaxed = true)
    private val preferencesManager: FingerprintPreferencesManager = mockk(relaxed = true)
    private val bluetoothAdapter: ComponentBluetoothAdapter = mockk()
    private val nfcManager: NfcManager = mockk()
    private val scannerFactory: ScannerFactory = mockk()

    private lateinit var viewModel: ConnectScannerViewModel

    @Before
    fun setUp() {
        declareModule {
            factory { mockk<FingerprintTimeHelper>(relaxed = true) }
            factory { sessionEventsManager }
            factory { fingerprintAnalyticsManager }
            factory { crashReportManager }
            factory { preferencesManager }
            factory { bluetoothAdapter }
            factory { nfcManager }
            factory { scannerFactory }
        }
        viewModel = get()
    }

    private fun mockScannerWrapper(connectFailException: Throwable? = null) = mockk<ScannerWrapper> {
        every { disconnect() } returns Completable.complete()
        every { connect() } returns if (connectFailException == null) Completable.complete() else Completable.error(connectFailException)
        every { setup() } returns Completable.complete()
        every { sensorWakeUp() } returns Completable.complete()
        every { setUiIdle() } returns Completable.complete()
        every { versionInformation() } returns mockk(relaxed = true)
    }

    @Test
    fun start_bluetoothOff_sendsBluetoothOffIssueEvent() {
        setupBluetooth(isEnabled = false)
        every { scannerFactory.create(any()) } returns mockScannerWrapper()

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.BluetoothOff)
    }

    @Test
    fun start_bluetoothNotSupported_sendsBluetoothNotSupportedAlert() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(connectFailException = BluetoothNotSupportedException())

        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun start_scannerConnectSucceeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper()

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerProgressObserver = viewModel.progress.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerProgressObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.NUMBER_OF_STEPS + 2) // 2 at the start
        verify { fingerprintAnalyticsManager.logScannerProperties(any(), any()) }
        verify { sessionEventsManager.addEventInBackground(any()) }
        verify { sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(any()) }
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcAvailableAndOff_sendsNfcOffIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = false)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcOff)
    }

    @Test
    fun start_noScannersPairedWithVero2WithNfcNotAvailable_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = false)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithVero1WithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_1)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_noScannersPairedWithMixedVeroGenerationsWithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 0)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_1, ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SerialEntryPair)
    }

    @Test
    fun start_multipleScannersPairedWithVero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupBluetooth(numberOfPairedScanners = 2)
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun start_scannerConnectFailsWithDisconnectedException_sendsScannerConnectedFailedEvent() {
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(ScannerDisconnectedException())

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        scannerConnectedObserver.assertEventReceivedWithContent(false)
    }

    @Test
    fun start_scannerConnectFailsWithUnexpectedException_sendsAlertEventAndLogsCrashlytics() {
        val error = Error("Oops")
        setupBluetooth(numberOfPairedScanners = 1)
        every { scannerFactory.create(any()) } returns mockScannerWrapper(error)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.start(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        scannerConnectedObserver.assertEventReceivedWithContent(false)
        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.UNEXPECTED_ERROR)
        verify { crashReportManager.logExceptionOrSafeException(eq(error)) }
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
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.handleScannerDisconnectedNoClick()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NfcPair)
    }

    @Test
    fun handleIncorrectScanner_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        setupNfc(doesDeviceHaveNfcCapability = true, isEnabled = true)
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

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
        every { bluetoothAdapter.getBondedDevices() } returns List(numberOfPairedScanners) { DummyBluetoothDevice.random() }.toSet()
    }

    private fun setupNfc(doesDeviceHaveNfcCapability: Boolean? = null, isEnabled: Boolean? = null) {
        if (doesDeviceHaveNfcCapability != null) {
            every { nfcManager.doesDeviceHaveNfcCapability() } returns doesDeviceHaveNfcCapability
        }
        if (isEnabled != null) {
            every { nfcManager.isNfcEnabled() } returns isEnabled
        }
    }
}
