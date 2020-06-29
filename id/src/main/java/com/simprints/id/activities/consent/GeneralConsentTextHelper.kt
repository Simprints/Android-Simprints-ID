package com.simprints.id.activities.consent

import android.content.Context
import com.google.gson.JsonSyntaxException
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.R
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.shortconsent.GeneralConsentOptions
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType

data class GeneralConsentTextHelper(val generalConsentOptionsJson: String,
                                    val programName: String,
                                    val organizationName: String,
                                    val modalities: List<Modality>,
                                    val crashReportManager: CrashReportManager,
                                    val context: Context) {

    private val generalConsentOptions by lazy { buildGeneralConsentOptions() }
    //First argument in consent text should always be program name, second is modality specific access/use case text
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
                append(context.getString(R.string.consent_enrol_only)
                    .format(programName, getModalitySpecificUseCaseText()))
            }
            if (consentEnrol) {
                append(context.getString(R.string.consent_enrol)
                    .format(programName, getModalitySpecificUseCaseText()))
            }
        }
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify() {
        if (generalConsentOptions.consentIdVerify) {
            append(context.getString(R.string.consent_id_verify)
                .format(programName, getModalitySpecificUseCaseText()))
        }
    }

    private fun StringBuilder.filterForDataSharingOptions() {
        with(generalConsentOptions) {
            if (consentShareDataNo) {
                append(context.getString(R.string.consent_share_data_no)
                    .format(getModalitySpecificAccessText()))
            }
            if (consentShareDataYes) {
                append(context.getString(R.string.consent_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText()))
            }
            if (consentCollectYes) {
                append(context.getString(R.string.consent_collect_yes))
            }
            if (consentPrivacyRights) {
                append(context.getString(R.string.consent_privacy_rights))
            }
            if (consentConfirmation) {
                append(context.getString(R.string.consent_confirmation)
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
        String.format("%s %s %s", context.getString(R.string.biometrics_general_fingerprint),
            context.getString(R.string.biometric_concat_modalities),
            context.getString(R.string.biometric_general_face))

    private fun getSingleModalitySpecificUseCaseText() =
        when (modalities.first()) {
            Modality.FACE -> context.getString(R.string.biometric_general_face)
            Modality.FINGER -> context.getString(R.string.biometrics_general_fingerprint)
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

    private fun buildGeneralConsentOptions() = try {
        JsonHelper.gson.fromJson(generalConsentOptionsJson, GeneralConsentOptions::class.java)
    } catch (e: JsonSyntaxException) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed General Consent Text Error", e))
        GeneralConsentOptions()
    }
}
