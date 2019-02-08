package com.simprints.id.activities

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.testUtils.di.AppModuleForTests
import com.simprints.id.testUtils.di.DaggerForUnitTests
import com.simprints.id.domain.consent.GeneralConsent
import com.simprints.id.domain.consent.ParentalConsent
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.shared.PreferencesModuleForAnyTests
import com.simprints.id.shared.mockSettingsPreferencesManager
import com.simprints.testframework.unit.reactive.RxJavaTest
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.id.testUtils.roboletric.RobolectricDaggerTestConfig
import com.simprints.testframework.unit.robolectric.RobolectricHelper
import junit.framework.TestCase.assertEquals
import kotlinx.android.synthetic.main.activity_launch.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class LaunchActivityTest : RxJavaTest, DaggerForUnitTests() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var settingsPreferencesManager: SettingsPreferencesManager
    @Inject lateinit var remoteDbManagerMock: RemoteDbManager

    override var preferencesModule by lazyVar {
        PreferencesModuleForAnyTests(
            settingsPreferencesManagerRule = MockRule
        )
    }

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            dbManagerRule = MockRule,
            scheduledPeopleSyncManagerRule = MockRule,
            scheduledSessionsSyncManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()
    }

    @Test
    fun enrollmentCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockSettingsPreferencesManagerForLaunchAct(parentalConsentExists = false)

        val calloutAction = CalloutAction.REGISTER
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        assertEquals("", parentConsentText)
    }

    @Test
    fun identifyCallout_showsCorrectGeneralConsentTextAndNoParentalByDefault() {
        mockSettingsPreferencesManagerForLaunchAct(parentalConsentExists = false)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        assertEquals("", parentConsentText)
    }

    @Test
    fun enrollmentCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockSettingsPreferencesManagerForLaunchAct(parentalConsentExists = true)

        val calloutAction = CalloutAction.REGISTER
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun identifyCallout_showsBothConsentsCorrectlyWhenParentalConsentExists() {
        mockSettingsPreferencesManagerForLaunchAct(parentalConsentExists = true)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)

        val parentConsentText = activity.parentalConsentTextView.text.toString()
        val defaultParentalConsentText = ParentalConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultParentalConsentText, parentConsentText)
    }

    @Test
    fun malformedConsentJson_showsDefaultConsent() {
        mockSettingsPreferencesManagerForLaunchAct(generalConsentOptions = MALFORMED_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val defaultGeneralConsentText = GeneralConsent().assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(defaultGeneralConsentText, generalConsentText)
    }

    @Test
    fun extraUnrecognisedConsentOptions_stillShowsCorrectValues() {
        mockSettingsPreferencesManagerForLaunchAct(generalConsentOptions = EXTRA_UNRECOGNISED_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = EXTRA_UNRECOGNISED_CONSENT_TARGET.assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(targetConsentText, generalConsentText)
    }

    @Test
    fun partiallyMissingConsentOptions_stillShowsCorrectValues() {
        mockSettingsPreferencesManagerForLaunchAct(generalConsentOptions = PARTIALLY_MISSING_CONSENT_OPTIONS)

        val calloutAction = CalloutAction.IDENTIFY
        preferencesManager.calloutAction = calloutAction

        val controller = createRoboLaunchActivity().start().resume().visible()
        val activity = controller.get()

        val generalConsentText = activity.generalConsentTextView.text.toString()
        val targetConsentText = PARTIALLY_MISSING_CONSENT_TARGET.assembleText(activity, calloutAction, PROGRAM_NAME, ORGANIZATION_NAME)
        assertEquals(targetConsentText, generalConsentText)
    }

    private fun mockSettingsPreferencesManagerForLaunchAct(parentalConsentExists: Boolean = false,
                                                           generalConsentOptions: String = REMOTE_CONSENT_GENERAL_OPTIONS,
                                                           parentalConsentOptions: String = REMOTE_CONSENT_PARENTAL_OPTIONS) {

        mockSettingsPreferencesManager(settingsPreferencesManager, parentalConsentExists, generalConsentOptions, parentalConsentOptions, LANGUAGE, PROGRAM_NAME, ORGANIZATION_NAME)
    }

    private fun createRoboLaunchActivity() = RobolectricHelper.createActivity<LaunchActivity>()

    companion object {
        private const val LANGUAGE = "en"
        private const val PROGRAM_NAME = "PROGRAM NAME"
        private const val ORGANIZATION_NAME = "ORGANIZATION NAME"
        private const val REMOTE_CONSENT_GENERAL_OPTIONS = "{\"consent_enrol_only\":false,\"consent_enrol\":true,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true}"
        private const val REMOTE_CONSENT_PARENTAL_OPTIONS = "{\"consent_parent_enrol_only\":false,\"consent_parent_enrol\":true,\"consent_parent_id_verify\":true,\"consent_parent_share_data_no\":true,\"consent_parent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_parent_privacy_rights\":true,\"consent_parent_confirmation\":true}"

        private const val MALFORMED_CONSENT_OPTIONS = "gibberish{\"000}\"\""

        private const val EXTRA_UNRECOGNISED_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false,\"consent_privacy_rights\":true,\"consent_confirmation\":true,\"this_one_doesnt_exist\":true}"
        private val EXTRA_UNRECOGNISED_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)

        private const val PARTIALLY_MISSING_CONSENT_OPTIONS = "{\"consent_enrol_only\":true,\"consent_enrol\":false,\"consent_id_verify\":true,\"consent_share_data_no\":true,\"consent_share_data_yes\":false,\"consent_collect_yes\":false}"
        private val PARTIALLY_MISSING_CONSENT_TARGET = GeneralConsent(consentEnrolOnly = true, consentEnrol = false)
    }
}
