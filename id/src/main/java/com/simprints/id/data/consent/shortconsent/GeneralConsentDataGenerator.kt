package com.simprints.id.data.consent.shortconsent

import com.simprints.id.R
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType
import com.simprints.id.tools.AndroidResourcesHelper

data class GeneralConsentDataGenerator(val generalConsentOptions: GeneralConsentOptions,
                                       val programName: String,
                                       val organizationName: String,
                                       val modalities: List<Modality>,
                                       val androidResourcesHelper: AndroidResourcesHelper) {

    fun assembleText(askConsentRequest: AskConsentRequest) = StringBuilder().apply {
        filterAppRequestForConsent(askConsentRequest)
        filterForDataSharingOptions()
    }.toString()

    private fun StringBuilder.filterAppRequestForConsent(askConsentRequest: AskConsentRequest) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForConsentEnrol()
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForConsentVerifyOrIdentify()
        }
    }

    private fun StringBuilder.appendTextForConsentEnrol() {
        with(generalConsentOptions) {
            if (consentEnrolOnly) {
                append(androidResourcesHelper.getString(R.string.consent_enrol_only)
                    .format(getModalitySpecificUseCaseText(), programName))
            }
            if (consentEnrol) {
                append(androidResourcesHelper.getString(R.string.consent_enrol)
                    .format(getModalitySpecificUseCaseText(), programName))
            }
        }
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify() {
        if (generalConsentOptions.consentIdVerify) {
            append(androidResourcesHelper.getString(R.string.consent_id_verify)
                .format(getModalitySpecificUseCaseText(), programName))
        }
    }

    private fun StringBuilder.filterForDataSharingOptions() {
        with(generalConsentOptions) {
            if (consentShareDataNo) {
                append(androidResourcesHelper.getString(R.string.consent_share_data_no)
                    .format(getModalitySpecificAccessText()))
            }
            if (consentShareDataYes) {
                append(androidResourcesHelper.getString(R.string.consent_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText()))
            }
            if (consentCollectYes) {
                append(androidResourcesHelper.getString(R.string.consent_collect_yes))
            }
            if (consentPrivacyRights) {
                append(androidResourcesHelper.getString(R.string.consent_privacy_rights))
            }
            if (consentConfirmation) {
                append(androidResourcesHelper.getString(R.string.consent_confirmation)
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
        String.format("%s %s %s", androidResourcesHelper.getString(R.string.biometrics_general_fingerprint),
            androidResourcesHelper.getString(R.string.biometric_concat_modalities),
            androidResourcesHelper.getString(R.string.biometric_general_face))

    private fun getSingleModalitySpecificUseCaseText() =
        when (modalities.first()) {
            Modality.FACE -> androidResourcesHelper.getString(R.string.biometric_general_face)
            Modality.FINGER -> androidResourcesHelper.getString(R.string.biometrics_general_fingerprint)
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
