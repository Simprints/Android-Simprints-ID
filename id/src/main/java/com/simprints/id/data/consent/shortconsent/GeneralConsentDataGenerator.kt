package com.simprints.id.data.consent.shortconsent

import android.content.Context
import com.simprints.id.R
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType

data class GeneralConsentDataGenerator(val generalConsentOptions: GeneralConsentOptions,
                                       val programName: String,
                                       val organizationName: String,
                                       val modality: Modality) {

    fun assembleText(context: Context, askConsentRequest: AskConsentRequest) = StringBuilder().apply {
        filterAppRequestForConsent(askConsentRequest, context)
        filterForDataSharingOptions(context)
    }.toString()

    private fun StringBuilder.filterAppRequestForConsent(askConsentRequest: AskConsentRequest, context: Context) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForConsentEnrol(context)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForConsentVerifyOrIdentify(context)
        }
    }

    private fun StringBuilder.appendTextForConsentEnrol(context: Context) {
        with (generalConsentOptions) {
            if (consentEnrolOnly) {
                append(context.getString(R.string.consent_enrol_only)
                    .format(getModalitySpecificUseCaseText(context), programName))
            }
            if (consentEnrol) {
                append(context.getString(R.string.consent_enrol)
                    .format(getModalitySpecificUseCaseText(context), programName))
            }
        }
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify(context: Context) {
        if (generalConsentOptions.consentIdVerify) {
            append(context.getString(R.string.consent_id_verify)
                .format(getModalitySpecificUseCaseText(context), programName))
        }
    }

    private fun StringBuilder.filterForDataSharingOptions(context: Context) {
        with (generalConsentOptions) {
            if (consentShareDataNo) {
                append(context.getString(R.string.consent_share_data_no)
                    .format(getModalitySpecificAccessText(context)))
            }
            if (consentShareDataYes) {
                append(context.getString(R.string.consent_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText(context)))
            }
            if (consentCollectYes) {
                append(context.getString(R.string.consent_collect_yes))
            }
            if (consentPrivacyRights) {
                append(context.getString(R.string.consent_privacy_rights))
            }
            if (consentConfirmation) {
                append(context.getString(R.string.consent_confirmation)
                    .format(getModalitySpecificUseCaseText(context)))
            }
        }
    }

    private fun getModalitySpecificUseCaseText(context: Context) = when (modality) {
        Modality.FACE -> context.getString(R.string.biometric_general_face)
        Modality.FINGER -> context.getString(R.string.biometrics_general_fingerprint)
    }

    private fun getModalitySpecificAccessText(context: Context) = when (modality) {
        Modality.FACE -> context.getString(R.string.biometrics_general_access_face)
        Modality.FINGER -> context.getString(R.string.biometrics_general_access_fingerprint)
    }
}
