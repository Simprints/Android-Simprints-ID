package com.simprints.id.activities.consent

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestPreferencesModule
import com.simprints.id.data.analytics.eventdata.controllers.local.SessionEventsLocalDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import com.simprints.testtools.unit.robolectric.createActivity
import kotlinx.android.synthetic.main.activity_consent.*
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
    @Inject lateinit var sessionEventsLocalDbManager: SessionEventsLocalDbManager

    private val preferencesModule by lazy {
        TestPreferencesModule(
            settingsPreferencesManagerRule = DependencyRule.SpyRule
        )
    }

    private val module by lazy {
        TestAppModule(app,
            dbManagerRule = DependencyRule.MockRule,
            sessionEventsLocalDbManagerRule = DependencyRule.MockRule,
            crashReportManagerRule = DependencyRule.MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module, preferencesModule).fullSetup()

        RobolectricTestMocker.setupSessionEventsManagerToAvoidRealmCall(sessionEventsLocalDbManager)
    }

    @Test
    fun consentDeclineOnMultipleModalities_shouldLaunchCoreExitFormActivity() {
        whenever(preferencesManagerSpy.modalities).thenReturn(listOf(Modality.FACE, Modality.FINGER))
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(CoreExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFingerprintModalityOnly_shouldLaunchFingerprintExitFormActivity() {
        whenever(preferencesManagerSpy.modalities).thenReturn(listOf(Modality.FINGER))
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FingerprintExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFaceModalityOnly_shouldLaunchCoreExitFormActivity() {
        whenever(preferencesManagerSpy.modalities).thenReturn(listOf(Modality.FACE))
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FaceExitFormActivity::class.java, activity)
    }

    private fun createRoboConsentActivity(intent: Intent) = createActivity<ConsentActivity>(intent)

    private fun getIntentForConsentAct() = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, AskConsentRequest(ConsentType.ENROL))
    }
}
