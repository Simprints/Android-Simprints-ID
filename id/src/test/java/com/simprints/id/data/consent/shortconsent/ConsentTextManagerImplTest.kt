package com.simprints.id.data.consent.shortconsent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.domain.moduleapi.app.requests.AppEnrolRequest
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ConsentTextManagerImplTest {

    val context = ApplicationProvider.getApplicationContext() as TestApplication
    companion object {
        private const val GENERAL_CONSENT_ENROL_TEXT = "I'd like to use your fingerprints to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I use your fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private  const val PARENTAL_CONSENT_ENROL_TEXT = "I'd like to use your child's fingerprints to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I use your child's fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
        private const val PROJECT_ID = "project_id"
        private const val USER_ID = "user_id"
        private const val MODULE_ID = "module_id"
    }

    @Test
    fun getGeneralConsentText_shouldReturnCorrectGeneralConsentText() {

        val consentDataManagerMock = mock<ConsentDataManager>().apply {
            whenever(this) { generalConsentOptionsJson } thenReturn JsonHelper.toJson(GeneralConsentOptions())
        }
        val consentTextManager = ConsentTextManagerImpl(context,
            consentDataManagerMock, mock(), PROGRAM_NAME, ORGANIZATION_NAME)

        val generalConsentText =
            consentTextManager.getGeneralConsentText(AppEnrolRequest(PROJECT_ID, USER_ID, MODULE_ID, "")).value

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_TEXT)
    }

    @Test
    fun getParentalConsentText_shouldReturnCorrectParentalConsentText() {

        val consentDataManagerMock = mock<ConsentDataManager>().apply {
            whenever(this) { parentalConsentOptionsJson } thenReturn JsonHelper.toJson(ParentalConsentOptions())
        }
        val consentTextManager = ConsentTextManagerImpl(context,
            consentDataManagerMock, mock(), PROGRAM_NAME, ORGANIZATION_NAME)

        val parentalConsentText =
            consentTextManager.getParentalConsentText(AppEnrolRequest(PROJECT_ID, USER_ID, MODULE_ID, "")).value

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_TEXT)
    }

    @Test
    fun parentalConsentExists_shouldReturnValueFromConsentDataManager() {
        val consentDataManagerMock = mock<ConsentDataManager>().apply {
            whenever(this) { parentalConsentExists } thenReturn true
        }
        val consentTextManager = ConsentTextManagerImpl(context,
            consentDataManagerMock, mock(), PROGRAM_NAME, ORGANIZATION_NAME)

        val parentalConsentExists = consentTextManager.parentalConsentExists().value

        assertThat(parentalConsentExists).isEqualTo(true)
    }

}
