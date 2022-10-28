package com.simprints.id.activities.consent

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.resources.R
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentalConsentTextHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val modalitiesUseCaseText = String.format(
        "%s %s %s", context.getString(R.string.biometrics_parental_fingerprint),
        context.getString(R.string.biometric_concat_modalities),
        context.getString(R.string.biometrics_parental_face)
    )

    companion object {
        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun `should return the correct consent for an enrol only with one modality`() {
        val request = AskConsentRequest(ConsentType.ENROL)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol_only)
            .format(PROGRAM_NAME, context.getString(R.string.biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an enrol only with two modalities`() {
        val request = AskConsentRequest(ConsentType.ENROL)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol_only)
            .format(PROGRAM_NAME, modalitiesUseCaseText)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a standard enrol with one modality`() {
        val request = AskConsentRequest(ConsentType.ENROL)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FACE)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol)
            .format(PROGRAM_NAME, context.getString(R.string.biometrics_parental_face))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a standard enrol with two modalities`() {
        val request = AskConsentRequest(ConsentType.ENROL)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol)
            .format(PROGRAM_NAME, modalitiesUseCaseText)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a verification`() {
        val request = AskConsentRequest(ConsentType.VERIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, context.getString(R.string.biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an identification with one modality`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, context.getString(R.string.biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an identification with two modalities`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, context.getString(R.string.biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for fingerprint`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.biometrics_access_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for face`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FACE)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.biometrics_access_face))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for face and fingerprint`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.biometrics_access_fingerprint_face))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is shared with partner for fingerprint`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = true,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_yes)
            .format(ORGANIZATION_NAME, context.getString(R.string.biometrics_access_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should add the correct string when the data is used for R&D`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = true,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_collect_yes)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the data is not used for R&D`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_collect_yes)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }

    @Test
    fun `should add the correct string when the privacy right is required`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = true,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_privacy_rights)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the privacy right is not required`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_privacy_rights)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }

    @Test
    fun `should add the correct string when the confirmation is required`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = true,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_confirmation)
            .format(context.getString(R.string.biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the confirmation is not required`() {
        val request = AskConsentRequest(ConsentType.IDENTIFY)
        val prompt = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
            dataSharedWithPartner = false,
            dataUsedForRAndD = false,
            privacyRights = false,
            confirmation = false,
        )
        val parentalConsentText = ParentalConsentTextHelper(
            prompt,
            PROGRAM_NAME,
            ORGANIZATION_NAME,
            listOf(GeneralConfiguration.Modality.FINGERPRINT)
        ).assembleText(request, context)

        val expectedString = context
            .getString(R.string.consent_parental_confirmation)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }
}
