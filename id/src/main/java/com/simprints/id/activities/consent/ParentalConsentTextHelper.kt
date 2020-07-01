package com.simprints.id.activities.consent

import android.content.Context
import com.google.gson.JsonSyntaxException
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
                                     val context: Context) {

    private val parentalConsentOptions by lazy {
        buildParentalConsentOptions()
    }

    //First argument in consent text should always be program name, second is modality specific access/use case text
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
                append(context.getString(R.string.consent_parental_enrol_only)
                    .format(programName, getModalitySpecificUseCaseText()))
            }
            if (consentParentEnrol) {
                append(context.getString(R.string.consent_parental_enrol)
                    .format(programName, getModalitySpecificUseCaseText()))
            }
        }
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify() {
        if (parentalConsentOptions.consentParentIdVerify) {
            append(context.getString(R.string.consent_parental_id_verify)
                .format(programName, getModalitySpecificUseCaseText()))
        }
    }

    private fun StringBuilder.extractDataSharingOptions() {
        with(parentalConsentOptions) {
            if (consentParentShareDataNo) {
                append(context.getString(R.string.consent_parental_share_data_no)
                    .format(getModalitySpecificAccessText()))
            }
            if (consentParentShareDataYes) {
                append(context.getString(R.string.consent_parental_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText()))
            }
            if (consentCollectYes) {
                append(context.getString(R.string.consent_collect_yes))
            }
            if (consentParentPrivacyRights) {
                append(context.getString(R.string.consent_parental_privacy_rights))
            }
            if (consentParentConfirmation) {
                append(context.getString(R.string.consent_parental_confirmation)
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
        String.format("%s %s %s", context.getString(R.string.biometrics_parental_fingerprint),
            context.getString(R.string.biometric_concat_modalities),
            context.getString(R.string.biometrics_parental_face))

    private fun getSingleModalitySpecificUseCaseText() =
        when (modalities.first()) {
            Modality.FACE -> context.getString(R.string.biometrics_parental_face)
            Modality.FINGER -> context.getString(R.string.biometrics_parental_fingerprint)
        }

    private fun getModalitySpecificAccessText() = if (isSingleModality()) {
        getSingleModalityAccessText()
    } else {
        getConcatenatedModalitiesAccessText()
    }

    private fun getConcatenatedModalitiesAccessText() =
        context.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText() = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.biometrics_access_face)
        Modality.FINGER -> context.getString(R.string.biometrics_access_fingerprint)
    }

    private fun isSingleModality() = modalities.size == 1

    private fun buildParentalConsentOptions() = try {
        JsonHelper.gson.fromJson(parentalConsentOptionsJson, ParentalConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed Parental Consent Text Error", e))
        ParentalConsentOptions()
    }
}
