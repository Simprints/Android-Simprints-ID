package com.simprints.fingerprint.activities.connect

import android.content.Intent
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
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.fingerprint.activities.alert.AlertActivity
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.connect.result.ConnectScannerTaskResult
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.testtools.FullAndroidTestConfigRule
import com.simprints.fingerprint.tools.livedata.postEvent
import com.simprints.id.Application
import com.simprints.testtools.android.tryOnSystemUntilTimeout
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.test.KoinTest
import org.koin.test.mock.declareModule

@RunWith(AndroidJUnit4::class)
class ConnectScannerActivityAndroidTest : KoinTest {

    @get:Rule
    var androidTestConfigRule = FullAndroidTestConfigRule()

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var scenario: ActivityScenario<ConnectScannerActivity>

    private val viewModelMock: ConnectScannerViewModel = mockk(relaxed = true)

    @Before
    fun setUp() {
        declareModule {
            viewModel { viewModelMock }
        }
    }

    @Test
    fun receivesFinishEvent_finishesActivityWithCorrectResult() {
        val finishLiveData = MutableLiveData<LiveDataEvent>()
        every { viewModelMock.finish } returns finishLiveData

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        finishLiveData.postEvent()

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
    fun pressBack_launchesRefusalActivity() {
        Intents.init()

        scenario = ActivityScenario.launch(connectScannerTaskRequest().toIntent())

        onView(isRoot()).perform(ViewActions.pressBack())

        intended(hasComponent(RefusalActivity::class.java.name))

        Intents.release()
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    companion object {
        private fun connectScannerTaskRequest() = ConnectScannerTaskRequest()

        private fun ConnectScannerTaskRequest.toIntent() = Intent().also {
            it.setClassName(ApplicationProvider.getApplicationContext<Application>().packageName, ConnectScannerActivity::class.qualifiedName!!)
            it.putExtra(ConnectScannerTaskRequest.BUNDLE_KEY, this)
        }
    }
}
