package com.simprints.id.activities.consent

import android.content.Context
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
                                    val jsonHelper: JsonHelper) {

    private val generalConsentOptions by lazy { buildGeneralConsentOptions() }
    //First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(askConsentRequest: AskConsentRequest, context: Context) = StringBuilder().apply {
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
        with(generalConsentOptions) {
            if (consentEnrolOnly) {
                append(context.getString(R.string.consent_enrol_only)
                    .format(programName, getModalitySpecificUseCaseText(context)))
            }
            if (consentEnrol) {
                append(context.getString(R.string.consent_enrol)
                    .format(programName, getModalitySpecificUseCaseText(context)))
            }
        }
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify(context: Context) {
        if (generalConsentOptions.consentIdVerify) {
            append(context.getString(R.string.consent_id_verify)
                .format(programName, getModalitySpecificUseCaseText(context)))
        }
    }

    private fun StringBuilder.filterForDataSharingOptions(context: Context) {
        with(generalConsentOptions) {
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

    private fun getModalitySpecificUseCaseText(context: Context) = if (isSingleModality()) {
        getSingleModalitySpecificUseCaseText(context)
    } else {
        getConcatenatedModalitiesUseCaseText(context)
    }

    private fun getConcatenatedModalitiesUseCaseText(context: Context) =
        String.format("%s %s %s", context.getString(R.string.biometrics_general_fingerprint),
            context.getString(R.string.biometric_concat_modalities),
            context.getString(R.string.biometric_general_face))

    private fun getSingleModalitySpecificUseCaseText(context: Context) =
        when (modalities.first()) {
            Modality.FACE -> context.getString(R.string.biometric_general_face)
            Modality.FINGER -> context.getString(R.string.biometrics_general_fingerprint)
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

    private fun buildGeneralConsentOptions() = try {
        jsonHelper.fromJson<GeneralConsentOptions>(generalConsentOptionsJson)
    } catch (e: Throwable) {
        crashReportManager.logExceptionOrSafeException(Exception("Malformed General Consent Text Error", e))
        GeneralConsentOptions()
    }
}
