package com.simprints.id.data.consent.shortconsent

import android.content.Context
import com.simprints.id.R
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.requests.ConsentType

data class ParentalConsentDataGenerator(val parentalConsentExists: Boolean,
                                        val parentalConsentOptions: ParentalConsentOptions,
                                        val programName: String,
                                        val organizationName: String,
                                        val modality: Modality) {

    fun assembleText(context: Context, askConsentRequest: AskConsentRequest) = StringBuilder().apply {
        filterAppRequestForParentalConsent(askConsentRequest, context)
        extractDataSharingOptions(context)
    }.toString()

    private fun StringBuilder.filterAppRequestForParentalConsent(askConsentRequest: AskConsentRequest, context: Context) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForParentalEnrol(context)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForIdentifyOrVerify(context)
        }
    }

    private fun StringBuilder.appendTextForParentalEnrol(context: Context) {
        if (parentalConsentOptions.consentParentEnrolOnly) {
            append(context.getString(R.string.consent_parental_enrol_only)
                .format(getModalitySpecificUseCaseText(context), programName))
        }
        if (parentalConsentOptions.consentParentEnrol) {
            append(context.getString(R.string.consent_parental_enrol)
                .format(getModalitySpecificUseCaseText(context), programName))
        }
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify(context: Context) {
        if (parentalConsentOptions.consentParentIdVerify) {
            append(context.getString(R.string.consent_parental_id_verify)
                .format(getModalitySpecificUseCaseText(context), programName))
        }
    }

    private fun StringBuilder.extractDataSharingOptions(context: Context) {
        if (parentalConsentOptions.consentParentShareDataNo) {
            append(context.getString(R.string.consent_parental_share_data_no))
        }
        if (parentalConsentOptions.consentParentShareDataYes) {
            append(context.getString(R.string.consent_parental_share_data_yes)
                .format(organizationName, getModalitySpecificAccessText(context)))
        }
        if (parentalConsentOptions.consentCollectYes) {
            append(context.getString(R.string.consent_collect_yes))
        }
        if (parentalConsentOptions.consentParentPrivacyRights) {
            append(context.getString(R.string.consent_parental_privacy_rights))
        }
        if (parentalConsentOptions.consentParentConfirmation) {
            append(context.getString(R.string.consent_parental_confirmation)
                .format(getModalitySpecificUseCaseText(context)))
        }
    }

    private fun getModalitySpecificUseCaseText(context: Context) = when (modality) {
        Modality.FACE -> context.getString(R.string.biometrics_parental_face)
        Modality.FINGER -> context.getString(R.string.biometrics_parental_fingerprint)
    }

    private fun getModalitySpecificAccessText(context: Context) = when (modality) {
        Modality.FACE -> context.getString(R.string.biometrics_general_access_face)
        Modality.FINGER -> context.getString(R.string.biometrics_general_access_fingerprint)
    }
}
