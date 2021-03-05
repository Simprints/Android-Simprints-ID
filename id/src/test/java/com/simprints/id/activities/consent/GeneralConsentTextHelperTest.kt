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
    private val request = AskConsentRequest(ConsentType.ENROL)


    private val fingerprintConsentText = "use your child's fingerprints"
    private val faceConsentText = "take photographs of your child's face"
    private val defaultGeneralConfigOptions = """
        {
          "consent_enrol_only": false,
          "consent_enrol": true,
          "consent_id_verify": true,
          "consent_share_data_no": false,
          "consent_share_data_yes": true,
          "consent_collect_yes": true,
          "consent_privacy_rights": true,
          "consent_confirmation": true
        }
        """.trimIndent()


    companion object {
        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun `should return consent text only containing consent for fingerprint modality when only fingerprint is used`() {
        // create text helper to assemble text
        val parentalConsentTextHelper = ParentalConsentTextHelper(
            defaultGeneralConfigOptions,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(Modality.FINGER),
            mockk(),
            JsonHelper()
        )

        // format entire object to get consent text message
        val parentalConsentText = parentalConsentTextHelper.assembleText(request, context)


        // assert that it contains fingerprint and not face consent
        assertThat(parentalConsentText).contains(fingerprintConsentText)
        assertThat(parentalConsentText).doesNotContain(faceConsentText)
    }

    @Test
    fun `should return consent text only containing consent for face modality when only face is used`() {
        // create text helper to assemble text
        val parentalConsentTextHelper = ParentalConsentTextHelper(
            defaultGeneralConfigOptions,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(Modality.FACE),
            mockk(),
            JsonHelper()
        )

        // format entire object to get consent text message
        val parentalConsentText = parentalConsentTextHelper.assembleText(request, context)


        // assert that it contains face and not fingerprint consent
        assertThat(parentalConsentText).contains(faceConsentText)
        assertThat(parentalConsentText).doesNotContain(fingerprintConsentText)
    }

    @Test
    fun `should return consent text containing consent for both fingerprint and face modalities, when both are used`() {
        val consentTextForBoth = "$fingerprintConsentText and $faceConsentText"

        // create text helper to assemble text
        val parentalConsentTextHelper = ParentalConsentTextHelper(
            defaultGeneralConfigOptions,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(Modality.FINGER, Modality.FACE),
            mockk(),
            JsonHelper()
        )

        // format entire object to get consent text message
        val parentalConsentText = parentalConsentTextHelper.assembleText(request, context)


        // assert that it contains consent text for both modalities
        assertThat(parentalConsentText).contains(consentTextForBoth)
    }
}
