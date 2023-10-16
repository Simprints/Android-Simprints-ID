package com.simprints.fingerprint.activities.connect

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.ScannerManagerImpl
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Vero2Configuration
import com.simprints.testtools.android.tryOnSystemUntilTimeout
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.mock.MockTimer
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConnectScannerActivityAndroidTest {

    private lateinit var scenario: ActivityScenario<ConnectScannerActivity>

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val mockTimer = MockTimer()
    private val timeHelper: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns mockk {
                every { vero2 } returns mockk {
                    every { qualityThreshold } returns 60
                    every { displayLiveFeedback } returns false
                    every { imageSavingStrategy } returns Vero2Configuration.ImageSavingStrategy.EAGER
                }
            }
        }
    }
    private val scanner: ScannerWrapper = mockk<ScannerWrapper>().apply {
        every { isLiveFeedbackAvailable() } returns false
        every { versionInformation() } returns mockk {
            every { generation } returns ScannerGeneration.VERO_2
        }
    }
    private val scannerManager: ScannerManager =
        spyk(ScannerManagerImpl(mockk(), mockk(), mockk(), mockk())) {
            coEvery { checkBluetoothStatus() } just Runs
        }
    private val nfcManager: NfcManager = mockk()

    private val viewModelMock: ConnectScannerViewModel = spyk(
        ConnectScannerViewModel(
            scannerManager,
            timeHelper,
            sessionEventsManager,
            mockk(relaxed = true),
            configManager,
            nfcManager,
        )
    ) {
        every { start() } just Runs
        connectMode = ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT
    }

//    @get:Rule
//    val koinTestRule = KoinTestRule(modules = listOf(module {
//        viewModel { viewModelMock }
//        single { mockk<FingerprintSessionEventsManager>(relaxed = true) }
//        single { mockk<FingerprintTimeHelper>(relaxed = true) }
//    }))

    @Before
    fun setUp() {
        every { scannerManager.scanner } returns scanner
    }

    @Test
    fun receivesFinishEvent_finishesActivityWithCorrectResult() {
        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        viewModelMock.finishConnectActivity()

        tryOnSystemUntilTimeout(2000, 200) {
            assertThat(scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
        }

        val result = scenario.result.resultData.run {
            setExtrasClassLoader(ConnectScannerTaskResult::class.java.classLoader)
            extras?.getParcelable<ConnectScannerTaskResult>(ConnectScannerTaskResult.BUNDLE_KEY)
        }

        assertThat(result).isNotNull()
    }

    @Test
    fun receivesAlertEvent_launchesAlertActivity() {
        val launchAlertLiveData = MutableLiveData<LiveDataEventWithContent<AlertError>>()
        every { viewModelMock.launchAlert } returns launchAlertLiveData

        Intents.init()

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        launchAlertLiveData.postEvent(AlertError.BLUETOOTH_NOT_SUPPORTED)

        intended(hasComponent("com.simprints.feature.alert.intent.AlertWrapperActivity"))

        Intents.release()
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    companion object {
        private fun connectScannerTaskRequest() =
            ConnectScannerTaskRequest(ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT)

        private fun ConnectScannerTaskRequest.toIntent() = Intent().also {
            it.setClassName(
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext,
                ConnectScannerActivity::class.qualifiedName!!
            )
            it.putExtra(ConnectScannerTaskRequest.BUNDLE_KEY, this)
        }
    }
}
