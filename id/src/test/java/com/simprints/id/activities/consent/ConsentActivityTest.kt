package com.simprints.id.activities.consent

import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.tabs.TabLayout
import com.google.common.truth.Truth.assertThat
import com.simprints.id.R
import com.simprints.id.activities.coreexitform.CoreExitFormActivity
import com.simprints.id.activities.faceexitform.FaceExitFormActivity
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormActivity
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.orchestrator.steps.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.testtools.TestApplication
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ConsentActivityTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private var modalities = emptyList<GeneralConfiguration.Modality>()

    private val consentConfiguration = mockk<ConsentConfiguration>()
    private val viewModel = mockk<ConsentViewModel>()

    @Before
    fun setUp() {
        every { viewModel.modalities } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<GeneralConfiguration.Modality>>>().onChanged(modalities)
            }
        }
        every { viewModel.consentConfiguration } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<ConsentConfiguration>>().onChanged(consentConfiguration)
            }
        }
    }

    @Test
    fun consentDeclineOnMultipleModalities_shouldLaunchCoreExitFormActivity() {
        modalities = listOf(
            GeneralConfiguration.Modality.FACE,
            GeneralConfiguration.Modality.FINGERPRINT
        )
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(CoreExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFingerprintModalityOnly_shouldLaunchFingerprintExitFormActivity() {
        modalities = listOf(GeneralConfiguration.Modality.FINGERPRINT)
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FingerprintExitFormActivity::class.java, activity)
    }

    @Test
    fun consentDeclineOnFaceModalityOnly_shouldLaunchCoreExitFormActivity() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.consentDeclineButton.performClick()

        assertActivityStarted(FaceExitFormActivity::class.java, activity)
    }

    @Test
    fun `declining on parental tab should still exit correctly`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.getTabAt(1)!!.select()
        activity.consentDeclineButton.performClick()

        assertActivityStarted(FaceExitFormActivity::class.java, activity)
    }

    @Test
    fun `declining on un-known tab should throw error`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.addTab(activity.tabHost.newTab(), 2, true)

        assertThrows<IllegalStateException> { activity.consentDeclineButton.performClick() }
    }

    @Test
    fun `general consent text should show first`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.programName } returns "this program"
        every { consentConfiguration.generalPrompt } returns ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        assertThat(activity.tabHost.selectedTabPosition).isEqualTo(0)
        assertThat(activity.consentTextHolderView.text.toString()).contains(GEN_CONSENT_HINT)
    }

    @Test
    fun `parental tab click should select correct tab`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.getTabAt(1)!!.select()

        activity.tabHost.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                assertThat(tab.position).isEqualTo(ConsentActivity.PARENTAL_CONSENT_TAB_TAG)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    @Test
    fun `general tab click should select correct tab`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.getTabAt(1)!!.select()
        activity.tabHost.getTabAt(0)!!.select()

        activity.tabHost.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                assertThat(tab.position).isEqualTo(ConsentActivity.GENERAL_CONSENT_TAB_TAG)
                assertThat(activity.consentTextHolderView.text.toString()).contains(GEN_CONSENT_HINT)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    @Test
    fun `selecting parental consent should set correct text`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        every { consentConfiguration.parentalPrompt } returns ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.getTabAt(1)!!.select()

        assertThat(activity.tabHost.selectedTabPosition).isEqualTo(1)
        assertThat(activity.consentTextHolderView.text.toString()).contains(PARENTAL_CONSENT_HINT)
    }

    @Test
    fun `re-selecting a tab shouldn't change the text`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true
        every { consentConfiguration.parentalPrompt } returns ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        activity.tabHost.getTabAt(1)!!.select()
        activity.tabHost.getTabAt(1)!!.select()

        assertThat(activity.tabHost.selectedTabPosition).isEqualTo(1)
        assertThat(activity.consentTextHolderView.text.toString()).contains(PARENTAL_CONSENT_HINT)
    }

    @Test
    fun `no parental consent should remove tab`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns false

        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        assertThat(activity.tabHost.tabCount).isEqualTo(1)
    }

    @Test
    fun `existing parental consent should leave tab`() {
        modalities = listOf(GeneralConfiguration.Modality.FACE)
        every { consentConfiguration.allowParentalConsent } returns true

        val controller = createRoboConsentActivity(getIntentForConsentAct())
        val activity = controller.get()

        assertThat(activity.tabHost.tabCount).isEqualTo(2)
    }

    private fun createRoboConsentActivity(intent: Intent) = createActivity<ConsentActivity>(intent)

    private fun getIntentForConsentAct() = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, AskConsentRequest(ConsentType.ENROL))
    }

    private val ConsentActivity.consentDeclineButton
        get() = findViewById<Button>(R.id.consentDeclineButton)

    private val ConsentActivity.tabHost
        get() = findViewById<TabLayout>(R.id.tabHost)

    private val ConsentActivity.consentTextHolderView
        get() = findViewById<TextView>(R.id.consentTextHolderView)

    companion object {
        private const val GEN_CONSENT_HINT =
            "I'd like to take photographs of your face to enrol you in this program and identify you"

        private const val PARENTAL_CONSENT_HINT =
            "I'd like to take photographs of your child's face to enrol them"
    }
}
