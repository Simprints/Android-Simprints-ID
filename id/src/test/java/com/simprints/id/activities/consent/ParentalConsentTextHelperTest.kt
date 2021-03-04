package com.simprints.id.activities.consent

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.consent.shortconsent.ParentalConsentOptions
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
class ParentalConsentTextHelperTest {

    private val context = ApplicationProvider.getApplicationContext() as TestApplication
    private val parentalConsentOptionsJson = JsonHelper().toJson(ParentalConsentOptions())
    private val request = AskConsentRequest(ConsentType.ENROL)

    companion object {
        private const val PARENTAL_CONSENT_ENROL_FINGER_TEXT = "I'd like to use your child's fingerprints to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their fingerprint information and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I use your child's fingerprints? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PARENTAL_CONSENT_ENROL_FACE_TEXT = "I'd like to take photographs of your child's face to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their photographs and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I take photographs of your child's face? Please say \"I accept\", \"I decline\", or \"I have questions.\""
        private const val PARENTAL_CONSENT_ENROL_MULTI_TEXT = "I'd like to use your child's fingerprints and take photographs of your child's face to enrol them in program_name and identify them in the future. Simprints, a UK-based nonprofit, will have access to their fingerprint information, photographs and current location. If you accept, you may withdraw your permission at any time and ask for your child's data to be erased. May I use your child's fingerprints and take photographs of your child's face? Please say \"I accept\", \"I decline\", or \"I have questions.\""

        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun getParentalConsentTextForFinger_shouldReturnCorrectParentalConsentText() {
        val parentalConsentText = ParentalConsentTextHelper(parentalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FINGER), mockk(), JsonHelper()).assembleText(request, context)

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_FINGER_TEXT)
    }

    @Test
    fun getParentalConsentTextForFace_shouldReturnCorrectParentalConsentText() {
        val parentalConsentText = ParentalConsentTextHelper(parentalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FACE), mockk(), JsonHelper()).assembleText(request, context)

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_FACE_TEXT)
    }

    @Test
    fun getParentalConsentTextForMultiModal_shouldReturnCorrectParentalConsentText() {
        val parentalConsentText = ParentalConsentTextHelper(parentalConsentOptionsJson,
            PROGRAM_NAME, ORGANIZATION_NAME, listOf(Modality.FINGER, Modality.FACE), mockk(), JsonHelper()).assembleText(request, context)

        assertThat(parentalConsentText).isEqualTo(PARENTAL_CONSENT_ENROL_MULTI_TEXT)
    }

    @Test
    fun `should return consent text containing associated 'consent_parent_collect_yes' string resource value, when consent_parent_collect_yes config flag is true`() {
        val associatedConsentText = "Simprints will also use the data for research purposes."

        // remote config - with consent_parent_collect_yes set to true
        val jsonConfigWithParentConsentTrue = """
            {
              "consent_parent_enrol_only": false,
              "consent_parent_enrol": true,
              "consent_parent_id_verify": true,
              "consent_parent_share_data_no": true,
              "consent_parent_share_data_yes": false,
              "consent_parent_collect_yes": true,
              "consent_parent_privacy_rights": true,
              "consent_parent_confirmation": true
            }
        """.trimIndent()

        // create text helper to assemble text
        val parentalConsentTextHelper = ParentalConsentTextHelper(
            jsonConfigWithParentConsentTrue,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(Modality.FACE),
            mockk(),
            JsonHelper()
        )

        // format entire object to get consent text message
        val parentalConsentText = parentalConsentTextHelper.assembleText(request, context)


        // assertion
        assertThat(parentalConsentText).contains(associatedConsentText)
    }

    @Test
    fun `should return consent text without associated 'consent_parent_collect_yes' string resource value, when consent_parent_collect_yes config flag is false`() {
        val associatedConsentText = "Simprints will also use the data for research purposes."

        // remote config - with consent_parent_collect_yes set to false
        val jsonConfigWithParentConsentTrue = """
            {
              "consent_parent_enrol_only": false,
              "consent_parent_enrol": true,
              "consent_parent_id_verify": true,
              "consent_parent_share_data_no": true,
              "consent_parent_share_data_yes": false,
              "consent_parent_collect_yes": false,
              "consent_parent_privacy_rights": true,
              "consent_parent_confirmation": true
            }
        """.trimIndent()

        // create text helper to assemble text
        val parentalConsentTextHelper = ParentalConsentTextHelper(
            jsonConfigWithParentConsentTrue,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(Modality.FACE),
            mockk(),
            JsonHelper()
        )

        // format entire object to get consent text message
        val parentalConsentText = parentalConsentTextHelper.assembleText(request, context)


        // assertion
        assertThat(parentalConsentText).doesNotContain(associatedConsentText)
    }
}
