package com.simprints.fingerprint.activities.connect

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.di.KoinInjector.acquireFingerprintKoinModules
import com.simprints.fingerprint.di.KoinInjector.releaseFingerprintKoinModules
import com.simprints.fingerprint.scanner.factory.ScannerFactory
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.UnitTestConfig
import com.simprints.fingerprint.testtools.assertEventReceivedWithContent
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.testtools.common.livedata.testObserver
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declareModule
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class ConnectScannerViewModelTest : KoinTest {

    private val preferencesManager: FingerprintPreferencesManager = mockk()
    private val bluetoothAdapter: ComponentBluetoothAdapter = mockk()
    private val nfcManager: NfcManager = mockk()
    private val scannerFactory: ScannerFactory = mockk()

    private lateinit var viewModel: ConnectScannerViewModel

    @Before
    fun setUp() {
        UnitTestConfig().fullSetup()
        startKoin {}
        acquireFingerprintKoinModules()
        declareModule {
            factory { mockk<FingerprintCrashReportManager>(relaxed = true) }
            factory { mockk<FingerprintTimeHelper>(relaxed = true) }
            factory {
                mockk<FingerprintSessionEventsManager>(relaxed = true) {
                    every { addEvent(any()) } returns Completable.complete()
                }
            }
            factory { mockk<FingerprintAnalyticsManager>(relaxed = true) }

            factory { preferencesManager }

            factory { bluetoothAdapter }
            factory { nfcManager }
            factory { scannerFactory }
        }

        viewModel = get()
    }

    private fun mockScannerWrapper(connectSucceeds: Boolean = true) = mockk<ScannerWrapper> {
        every { disconnect() } returns Completable.complete()
        every { connect() } returns Completable.complete()
        every { sensorWakeUp() } returns Completable.complete()
        every { setUiIdle() } returns Completable.complete()
    }

    @Test
    fun bluetoothOff_start_sendsBluetoothOffIssueEvent() {
        every { bluetoothAdapter.isEnabled() } returns false
        every { scannerFactory.create(any()) } returns mockScannerWrapper()

        val connectScannerIssueObserver = viewModel.connectScannerIssue.testObserver()

        viewModel.start()

        connectScannerIssueObserver.assertEventReceivedWithContent(ConnectScannerIssue.BLUETOOTH_OFF)
    }

    @After
    fun tearDown() {
        releaseFingerprintKoinModules()
        stopKoin()
    }
}
