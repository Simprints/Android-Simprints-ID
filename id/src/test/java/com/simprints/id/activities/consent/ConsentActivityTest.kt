package com.simprints.id.activities.consent

import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.R
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule.MockkRule
import com.simprints.testtools.common.di.DependencyRule.SpykRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.every
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject


@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ConsentActivityTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject lateinit var preferencesManagerSpy: PreferencesManager

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = SpykRule
        )
    }

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = MockkRule,
            sessionEventsLocalDbManagerRule = MockkRule,
            crashReportManagerRule = MockkRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module, preferencesModule).fullSetup()
    }

    @Test
    fun consentDeclineOnMultipleModalities_shouldLaunchCoreExitFormActivity() {
        every { preferencesManagerSpy.modalities } returns listOf(Modality.FACE, Modality.FINGER)
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(CoreExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFingerprintModalityOnly_shouldLaunchFingerprintExitFormActivity() {
        every { preferencesManagerSpy.modalities } returns listOf(Modality.FINGER)
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FingerprintExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFaceModalityOnly_shouldLaunchCoreExitFormActivity() {
        every { preferencesManagerSpy.modalities } returns listOf(Modality.FACE)
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FaceExitFormActivity::class.java, activity)
    }

    private fun createRoboConsentActivity(intent: Intent) = createActivity<ConsentActivity>(intent)

    private fun getIntentForConsentAct() = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, AskConsentRequest(ConsentType.ENROL))
    }

    private val ConsentActivity.consentDeclineButton
        get() = findViewById<Button>(R.id.consentDeclineButton)
}
