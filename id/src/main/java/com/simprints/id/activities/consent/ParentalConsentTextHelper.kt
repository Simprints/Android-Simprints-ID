package com.simprints.id.activities.consent

import android.content.Context
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.shortconsent.ParentalConsentOptions
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType

data class ParentalConsentTextHelper(val parentalConsentOptionsJson: String,
                                     val programName: String,
                                     val organizationName: String,
                                     val modalities: List<Modality>,
                                     val crashReportManager: CrashReportManager,
                                     val jsonHelper: JsonHelper) {

    private val parentalConsentOptions by lazy {
        buildParentalConsentOptions()
    }

    //First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(askConsentRequest: AskConsentRequest, context: Context) = StringBuilder().apply {
        filterAppRequestForParentalConsent(askConsentRequest, context)
        extractDataSharingOptions(context)
    }.toString()

    private fun StringBuilder.filterAppRequestForParentalConsent(askConsentRequest: AskConsentRequest,
                                                                 context: Context) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForParentalEnrol(context)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForIdentifyOrVerify(context)
        }
    }

    private fun StringBuilder.appendTextForParentalEnrol(context: Context) {
        with(parentalConsentOptions) {
            if (consentParentEnrolOnly) {
                append(context.getString(R.string.consent_parental_enrol_only)
                    .format(programName, getModalitySpecificUseCaseText(context)))
            }
            if (consentParentEnrol) {
                append(context.getString(R.string.consent_parental_enrol)
                    .format(programName, getModalitySpecificUseCaseText(context)))
            }
        }
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify(context: Context) {
        if (parentalConsentOptions.consentParentIdVerify) {
            append(context.getString(R.string.consent_parental_id_verify)
                .format(programName, getModalitySpecificUseCaseText(context)))
        }
    }

    private fun StringBuilder.extractDataSharingOptions(context: Context) {
        with(parentalConsentOptions) {
            if (consentParentShareDataNo) {
                append(context.getString(R.string.consent_parental_share_data_no)
                    .format(getModalitySpecificAccessText(context)))
            }
            if (consentParentShareDataYes) {
                append(context.getString(R.string.consent_parental_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText(context)))
            }
            if (consentParentCollectYes) {
                append(context.getString(R.string.consent_collect_yes))
            }
            if (consentParentPrivacyRights) {
                append(context.getString(R.string.consent_parental_privacy_rights))
            }
            if (consentParentConfirmation) {
                append(context.getString(R.string.consent_parental_confirmation)
                    .format(getModalitySpecificUseCaseText(context)))
            }
        }
    }

    private fun getModalitySpecificUseCaseText(context: Context) = if (isSingleModality()) {
        getSingleModalitySpecificUseCaseText(context)
    } else {
        getConcatenatedModalitiesUseCaseText(context)
    }

    private fun getConcatenatedModalitiesUseCaseText(context: Context) =
        String.format("%s %s %s", context.getString(R.string.biometrics_parental_fingerprint),
            context.getString(R.string.biometric_concat_modalities),
            context.getString(R.string.biometrics_parental_face))

    private fun getSingleModalitySpecificUseCaseText(context: Context) =
        when (modalities.first()) {
            Modality.FACE -> context.getString(R.string.biometrics_parental_face)
            Modality.FINGER -> context.getString(R.string.biometrics_parental_fingerprint)
        }

    private fun getModalitySpecificAccessText(context: Context) = if (isSingleModality()) {
        getSingleModalityAccessText(context)
    } else {
        getConcatenatedModalitiesAccessText(context)
    }

    private fun getConcatenatedModalitiesAccessText(context: Context) =
        context.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText(context: Context) = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.biometrics_access_face)
        Modality.FINGER -> context.getString(R.string.biometrics_access_fingerprint)
    }

    private fun isSingleModality() = modalities.size == 1

    private fun buildParentalConsentOptions() = try {
        jsonHelper.fromJson<ParentalConsentOptions>(parentalConsentOptionsJson)
    } catch (e: Throwable) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed Parental Consent Text Error", e))
        ParentalConsentOptions()
    }
}
