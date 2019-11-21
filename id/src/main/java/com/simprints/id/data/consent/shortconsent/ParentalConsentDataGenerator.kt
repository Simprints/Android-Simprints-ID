package com.simprints.id.data.consent.shortconsent

import com.simprints.id.R
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.tools.AndroidResourcesHelper

data class ParentalConsentDataGenerator(val parentalConsentExists: Boolean,
                                        val parentalConsentOptions: ParentalConsentOptions,
                                        val programName: String,
                                        val organizationName: String,
                                        val modalities: List<Modality>,
                                        val androidResourcesHelper: AndroidResourcesHelper) {

    fun assembleText(askConsentRequest: AskConsentRequest) = StringBuilder().apply {
        filterAppRequestForParentalConsent(askConsentRequest)
        extractDataSharingOptions()
    }.toString()

    private fun StringBuilder.filterAppRequestForParentalConsent(askConsentRequest: AskConsentRequest) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForParentalEnrol()
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForIdentifyOrVerify()
        }
    }

    private fun StringBuilder.appendTextForParentalEnrol() {
        with(parentalConsentOptions) {
            if (consentParentEnrolOnly) {
                append(androidResourcesHelper.getString(R.string.consent_parental_enrol_only)
                    .format(getModalitySpecificUseCaseText(), programName))
            }
            if (consentParentEnrol) {
                append(androidResourcesHelper.getString(R.string.consent_parental_enrol)
                    .format(getModalitySpecificUseCaseText(), programName))
            }
        }
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify() {
        if (parentalConsentOptions.consentParentIdVerify) {
            append(androidResourcesHelper.getString(R.string.consent_parental_id_verify)
                .format(getModalitySpecificUseCaseText(), programName))
        }
    }

    private fun StringBuilder.extractDataSharingOptions() {
        with(parentalConsentOptions) {
            if (consentParentShareDataNo) {
                append(androidResourcesHelper.getString(R.string.consent_parental_share_data_no)
                    .format(getModalitySpecificAccessText()))
            }
            if (consentParentShareDataYes) {
                append(androidResourcesHelper.getString(R.string.consent_parental_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText()))
            }
            if (consentCollectYes) {
                append(androidResourcesHelper.getString(R.string.consent_collect_yes))
            }
            if (consentParentPrivacyRights) {
                append(androidResourcesHelper.getString(R.string.consent_parental_privacy_rights))
            }
            if (consentParentConfirmation) {
                append(androidResourcesHelper.getString(R.string.consent_parental_confirmation)
                    .format(getModalitySpecificUseCaseText()))
            }
        }
    }

    private fun getModalitySpecificUseCaseText() = if (isSingleModality()) {
        getSingleModalitySpecificUseCaseText()
    } else {
        getConcatenatedModalitiesUseCaseText()
    }

    private fun getConcatenatedModalitiesUseCaseText() =
        String.format("%s %s %s", androidResourcesHelper.getString(R.string.biometrics_parental_fingerprint),
            androidResourcesHelper.getString(R.string.biometric_concat_modalities),
            androidResourcesHelper.getString(R.string.biometrics_parental_face))

    private fun getSingleModalitySpecificUseCaseText() =
        when (modalities.first()) {
            Modality.FACE -> androidResourcesHelper.getString(R.string.biometrics_parental_face)
            Modality.FINGER -> androidResourcesHelper.getString(R.string.biometrics_parental_fingerprint)
        }

    private fun getModalitySpecificAccessText() = if (isSingleModality()) {
        getSingleModalityAccessText()
    } else {
        getConcatenatedModalitiesAccessText()
    }

    private fun getConcatenatedModalitiesAccessText() =
        androidResourcesHelper.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText() = when (modalities.first()) {
        Modality.FACE -> androidResourcesHelper.getString(R.string.biometrics_access_face)
        Modality.FINGER -> androidResourcesHelper.getString(R.string.biometrics_access_fingerprint)
    }

    private fun isSingleModality() = modalities.size == 1
}
