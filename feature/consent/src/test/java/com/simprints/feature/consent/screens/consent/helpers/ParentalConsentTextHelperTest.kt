package com.simprints.feature.consent.screens.consent.helpers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.feature.consent.ConsentType
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.resources.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentalConsentTextHelperTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val modalitiesUseCaseText = String.format(
        "%s %s %s", context.getString(R.string.consent_biometrics_parental_fingerprint),
        context.getString(R.string.consent_biometric_concat_modalities),
        context.getString(R.string.consent_biometrics_parental_face)
    )

    companion object {
        private const val PROGRAM_NAME = "program_name"
        private const val ORGANIZATION_NAME = "organization_name"
    }

    @Test
    fun `should return the correct consent for an enrol only with one modality`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.ENROL,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol_only)
            .format(PROGRAM_NAME, context.getString(R.string.consent_biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an enrol only with two modalities`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE),
            ConsentType.ENROL,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol_only)
            .format(PROGRAM_NAME, modalitiesUseCaseText)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a standard enrol with one modality`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FACE),
            ConsentType.ENROL,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol)
            .format(PROGRAM_NAME, context.getString(R.string.consent_biometrics_parental_face))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a standard enrol with two modalities`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE),
            ConsentType.ENROL,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_enrol)
            .format(PROGRAM_NAME, modalitiesUseCaseText)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for a verification`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.VERIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, context.getString(R.string.consent_biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an identification with one modality`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, context.getString(R.string.consent_biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should return the correct consent for an identification with two modalities`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_id_verify)
            .format(PROGRAM_NAME, modalitiesUseCaseText)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for fingerprint`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.consent_biometrics_access_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for face`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FACE),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.consent_biometrics_access_face))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is not shared with partner for face and fingerprint`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT, GeneralConfiguration.Modality.FACE),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_no)
            .format(context.getString(R.string.consent_biometrics_access_fingerprint_face))

        assertThat(parentalConsentText).contains(expectedString)
        assertThat(parentalConsentText).doesNotContain(ORGANIZATION_NAME)
    }

    @Test
    fun `should add the correct string when the data is shared with partner for fingerprint`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = true,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_share_data_yes)
            .format(ORGANIZATION_NAME, context.getString(R.string.consent_biometrics_access_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should add the correct string when the data is used for R&D`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = true,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_collect_yes)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the data is not used for R&D`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_collect_yes)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }

    @Test
    fun `should add the correct string when the privacy right is required`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = true,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_privacy_rights)

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the privacy right is not required`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_privacy_rights)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }

    @Test
    fun `should add the correct string when the confirmation is required`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = true,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_confirmation)
            .format(context.getString(R.string.consent_biometrics_parental_fingerprint))

        assertThat(parentalConsentText).contains(expectedString)
    }

    @Test
    fun `should not add the string when the confirmation is not required`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = false,
                    dataUsedForRAndD = false,
                    privacyRights = false,
                    confirmation = false,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        val expectedString = context
            .getString(R.string.consent_parental_confirmation)

        assertThat(parentalConsentText).doesNotContain(expectedString)
    }

    @Test
    fun `should not start a new sentence after a period without a following space`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = true,
                    dataUsedForRAndD = true,
                    privacyRights = true,
                    confirmation = true,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        assertThat(parentalConsentText).doesNotContainMatch("\\.\\w")
    }

    @Test
    fun `should not start with a space`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = true,
                    dataUsedForRAndD = true,
                    privacyRights = true,
                    confirmation = true,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        assertThat(parentalConsentText).doesNotContainMatch("^\\s.*")
    }

    @Test
    fun `should not contain double spaces`() {
        val parentalConsentText = ParentalConsentTextHelper(
            configWithPrompt(
                ConsentConfiguration.ConsentPromptConfiguration(
                    enrolmentVariant = ConsentConfiguration.ConsentEnrolmentVariant.STANDARD,
                    dataSharedWithPartner = true,
                    dataUsedForRAndD = true,
                    privacyRights = true,
                    confirmation = true,
                )
            ),
            listOf(GeneralConfiguration.Modality.FINGERPRINT),
            ConsentType.IDENTIFY,
        ).assembleText(context)

        assertThat(parentalConsentText).doesNotContain("  ")
    }

    private fun configWithPrompt(prompt: ConsentConfiguration.ConsentPromptConfiguration) = ConsentConfiguration(
        programName = PROGRAM_NAME,
        organizationName = ORGANIZATION_NAME,
        generalPrompt = null,
        collectConsent = true,
        displaySimprintsLogo = true,
        allowParentalConsent = true,
        parentalPrompt = prompt,
    )
}
