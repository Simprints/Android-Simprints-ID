package com.simprints.fingerprint.activities.connect

import android.content.Intent
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.activities.alert.AlertActivity
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.commontesttools.time.MockTimer
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.data.domain.images.isImageTransferRequired
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.testtools.FullAndroidTestConfigRule
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.id.Application
import com.simprints.testtools.android.tryOnSystemUntilTimeout
import io.mockk.*
import io.reactivex.Completable
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest


@RunWith(AndroidJUnit4::class)
class ConnectScannerActivityAndroidTest : KoinTest {

    @get:Rule
    var androidTestConfigRule = FullAndroidTestConfigRule()

    @get:Rule
    var permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<ConnectScannerActivity>

    private lateinit var viewModelMock: ConnectScannerViewModel


    private val mockTimer = MockTimer()
    private val timeHelper: FingerprintTimeHelper = mockk(relaxed = true) {
        every { newTimer() } returns mockTimer
    }
    private val sessionEventsManager: FingerprintSessionEventsManager = mockk(relaxed = true)
    private val preferencesManager: FingerprintPreferencesManager = mockk(relaxed = true) {
        every { qualityThreshold } returns 60
        every { liveFeedbackOn } returns false
        every { saveFingerprintImagesStrategy.isImageTransferRequired() } returns true
    }
    private val scanner: ScannerWrapper = mockk<ScannerWrapper>().apply {
        every { isLiveFeedbackAvailable() } returns false
    }
    private val scannerManager: ScannerManager =
        spyk(ScannerManagerImpl(mockk(), mockk(), mockk(), mockk())) {
            every { checkBluetoothStatus() } returns Completable.complete()
        }
    private val nfcManager: NfcManager = mockk()

    @Before
    fun setUp() {
        scannerManager.scanner = scanner

        viewModelMock = spyk(
            ConnectScannerViewModel(
                scannerManager, timeHelper, sessionEventsManager, preferencesManager, nfcManager
            )
        ) {
            every { start() } just Runs
            every { init(any()) } just Runs
            connectMode = ConnectScannerTaskRequest.ConnectMode.INITIAL_CONNECT
        }
        loadKoinModules(module {
            viewModel { viewModelMock }
        })
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
        val launchAlertLiveData = MutableLiveData<LiveDataEventWithContent<FingerprintAlert>>()
        every { viewModelMock.launchAlert } returns launchAlertLiveData

        Intents.init()

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        launchAlertLiveData.postEvent(FingerprintAlert.BLUETOOTH_NOT_SUPPORTED)

        intended(hasComponent(AlertActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun pressBack_handlesAPILevel() {
        val backButtonBehaviourLiveData =
            MutableLiveData(ConnectScannerViewModel.BackButtonBehaviour.EXIT_FORM)
        every { viewModelMock.backButtonBehaviour } returns backButtonBehaviourLiveData

        Intents.init()

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(isRoot()).perform(ViewActions.pressBack())

        /**
         * If the API is above 31 the back button will exit the permissions dialog
         */
        if (Build.VERSION.SDK_INT < 31)
            intended(hasComponent(RefusalActivity::class.java.name))
        else
            intended(hasComponent(AlertActivity::class.java.name))

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
                ApplicationProvider.getApplicationContext<Application>().packageName,
                ConnectScannerActivity::class.qualifiedName!!
            )
            it.putExtra(ConnectScannerTaskRequest.BUNDLE_KEY, this)
        }
    }
}
