package com.simprints.id.activities.consent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.consent.shortconsent.GeneralConsentOptions
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class GeneralConsentTextHelperTest {

    private val context = ApplicationProvider.getApplicationContext() as TestApplication
    private val generalConsentOptionsJson = JsonHelper.toJson(GeneralConsentOptions())
    private val request = AskConsentRequest(ConsentType.ENROL)

    companion object {
        private const val GENERAL_CONSENT_ENROL_FINGER_TEXT = "I'd like to use your fingerprints to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I use your fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val GENERAL_CONSENT_ENROL_FACE_TEXT = "I'd like to take photographs of your face to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your photographs and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I take photographs of your face? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val GENERAL_CONSENT_ENROL_MULTI_TEXT = "I'd like to use your fingerprints and take photographs of your face to enrol you in program_name and identify you in the future. Simprints, a UK-based nonprofit, will have access to your fingerprint information, photographs and current location. If you accept, you may withdraw your permission at any time and ask for your data to be erased. May I use your fingerprints and take photographs of your face? Please say \"I accept\", \"I decline\", or \"I have questions.\""

        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun getGeneralConsentTextForFinger_shouldReturnCorrectGeneralConsentText() {
        val generalConsentText = GeneralConsentTextHelper(generalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FINGER), mockk()).assembleText(request)

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_FINGER_TEXT)
    }

    @Test
    fun getGeneralConsentTextForFace_shouldReturnCorrectGeneralConsentText() {
        val generalConsentOptionsJson = JsonHelper.toJson(GeneralConsentOptions())
        val request = AskConsentRequest(ConsentType.ENROL)

        val generalConsentText = GeneralConsentTextHelper(generalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FACE), mockk()).assembleText(request)

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_FACE_TEXT)
    }

    @Test
    fun getGeneralConsentTextForMultiModal_shouldReturnCorrectGeneralConsentText() {
        val generalConsentText = GeneralConsentTextHelper(generalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FINGER, Modality.FACE), mockk()).assembleText(request)

        assertThat(generalConsentText).isEqualTo(GENERAL_CONSENT_ENROL_MULTI_TEXT)
    }
}
