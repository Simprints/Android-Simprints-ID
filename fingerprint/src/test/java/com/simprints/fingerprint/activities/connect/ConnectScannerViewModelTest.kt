package com.simprints.fingerprint.activities.connect

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.scanner.exceptions.unexpected.BluetoothNotSupportedException
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullUnitTestConfigRule
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
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declareModule
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class ConnectScannerViewModelTest : KoinTest {

    @get:Rule
    var unitTestConfigRule = FullUnitTestConfigRule()

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
        every { sensorWakeUp() } returns Completable.complete()
        every { setUiIdle() } returns Completable.complete()
        every { versionInformation() } returns mockk(relaxed = true)
    }

    @Test
    fun bluetoothOff_start_sendsBluetoothOffIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns false
        every { scannerFactory.create(any()) } returns mockScannerWrapper()

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.BLUETOOTH_OFF)
    }

    @Test
    fun bluetoothNotSupported_start_sendsBluetoothNotSupportedAlert() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns setOf(DummyBluetoothDevice.random())
        every { scannerFactory.create(any()) } returns mockScannerWrapper(connectFailException = BluetoothNotSupportedException())

        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.start()

        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.BLUETOOTH_NOT_SUPPORTED)
    }

    @Test
    fun scannerConnect_succeeds_sendsScannerConnectedEventAndProgressValuesAndLogsPropertiesAndSessionEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns setOf(DummyBluetoothDevice.random())
        every { scannerFactory.create(any()) } returns mockScannerWrapper()

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val scannerProgressObserver = viewModel.progress.testObserver()

        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(true)
        assertThat(scannerProgressObserver.observedValues.size).isEqualTo(ConnectScannerViewModel.NUMBER_OF_STEPS + 2) // 2 at the start
        verify { fingerprintAnalyticsManager.logScannerProperties(any(), any()) }
        verify { sessionEventsManager.addEventInBackground(any()) }
        verify { sessionEventsManager.updateHardwareVersionInScannerConnectivityEvent(any()) }
    }

    @Test
    fun noScannersPaired_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns emptySet()
        every { nfcManager.doesDeviceHaveNfcCapability() } returns true
        every { nfcManager.isNfcEnabled() } returns true
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NFC_PAIR)
    }

    @Test
    fun noScannersPaired_vero2WithNfcAvailableAndOff_sendsNfcOffIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns emptySet()
        every { nfcManager.doesDeviceHaveNfcCapability() } returns true
        every { nfcManager.isNfcEnabled() } returns false
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NFC_OFF)
    }

    @Test
    fun noScannersPaired_vero2WithNfcNotAvailable_sendsSerialEntryIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns emptySet()
        every { nfcManager.doesDeviceHaveNfcCapability() } returns false
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SERIAL_ENTRY_PAIR)
    }

    @Test
    fun noScannersPaired_vero1WithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns emptySet()
        every { nfcManager.doesDeviceHaveNfcCapability() } returns true
        every { nfcManager.isNfcEnabled() } returns true
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_1)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SERIAL_ENTRY_PAIR)
    }

    @Test
    fun noScannersPaired_mixedVeroGenerationsWithNfcAvailableAndOn_sendsSerialEntryIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns emptySet()
        every { nfcManager.doesDeviceHaveNfcCapability() } returns true
        every { nfcManager.isNfcEnabled() } returns true
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_1, ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.SERIAL_ENTRY_PAIR)
    }

    @Test
    fun multipleScannersPaired_vero2WithNfcAvailableAndOn_sendsNfcPairIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns setOf(DummyBluetoothDevice.random(), DummyBluetoothDevice.random())
        every { nfcManager.doesDeviceHaveNfcCapability() } returns true
        every { nfcManager.isNfcEnabled() } returns true
        every { preferencesManager.scannerGenerations } returns listOf(ScannerGeneration.VERO_2)

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.NFC_PAIR)
    }

    @Test
    fun scannerConnect_fails_sendsScannerConnectedFailedEvent() {
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns setOf(DummyBluetoothDevice.random())
        every { scannerFactory.create(any()) } returns mockScannerWrapper(ScannerDisconnectedException())

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()

        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
    }

    @Test
    fun scannerConnect_failsWithUnexpectedException_sendsAlertEventAndLogsCrashlytics() {
        val error = Error("Oops")
        every { bluetoothAdapter.isEnabled() } returns true
        every { bluetoothAdapter.getBondedDevices() } returns setOf(DummyBluetoothDevice.random())
        every { scannerFactory.create(any()) } returns mockScannerWrapper(error)

        val scannerConnectedObserver = viewModel.scannerConnected.testObserver()
        val launchAlertObserver = viewModel.launchAlert.testObserver()

        viewModel.start()

        scannerConnectedObserver.assertEventReceivedWithContent(false)
        launchAlertObserver.assertEventReceivedWithContent(FingerprintAlert.UNEXPECTED_ERROR)
        verify { crashReportManager.logExceptionOrSafeException(eq(error)) }
    }
}
