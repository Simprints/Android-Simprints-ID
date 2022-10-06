package com.simprints.id.activities.consent

import android.content.Context
import com.simprints.core.domain.modality.Modality
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infraresources.R
import com.simprints.id.data.consent.shortconsent.GeneralConsentOptions
import com.simprints.id.orchestrator.steps.core.requests.AskConsentRequest
import com.simprints.id.orchestrator.steps.core.requests.ConsentType
import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.GeneralConfiguration

data class GeneralConsentTextHelper(
    val prompt: ConsentConfiguration.ConsentPromptConfiguration,
    val programName: String,
    val organizationName: String,
    val modalities: List<GeneralConfiguration.Modality>,
) {
    //First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(askConsentRequest: AskConsentRequest, context: Context) =
        StringBuilder().apply {
            filterAppRequestForConsent(askConsentRequest, context)
            filterForDataSharingOptions(context)
        }.toString()

    private fun StringBuilder.filterAppRequestForConsent(
        askConsentRequest: AskConsentRequest,
        context: Context
    ) {
        when (askConsentRequest.consentType) {
            ConsentType.ENROL -> appendTextForConsentEnrol(context)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForConsentVerifyOrIdentify(context)
        }
    }

    private fun StringBuilder.appendTextForConsentEnrol(context: Context) {
        if (prompt.enrolmentVariant == ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY) {
            append(
                context.getString(R.string.consent_enrol_only)
                    .format(programName, getModalitySpecificUseCaseText(context))
            )
        }
        if (prompt.enrolmentVariant == ConsentConfiguration.ConsentEnrolmentVariant.STANDARD) {
            append(
                context.getString(R.string.consent_enrol)
                    .format(programName, getModalitySpecificUseCaseText(context))
            )
        }
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify(context: Context) {
        append(
            context.getString(R.string.consent_id_verify)
                .format(programName, getModalitySpecificUseCaseText(context))
        )
    }

    private fun StringBuilder.filterForDataSharingOptions(context: Context) {
        if (prompt.dataSharedWithPartner) {
            append(
                context.getString(R.string.consent_share_data_yes)
                    .format(organizationName, getModalitySpecificAccessText(context))
            )
        } else {
            append(
                context.getString(R.string.consent_share_data_no)
                    .format(getModalitySpecificAccessText(context))
            )
        }
        if (prompt.dataUsedForRAndD) {
            append(context.getString(R.string.consent_collect_yes))
        }
        if (prompt.privacyRights) {
            append(context.getString(R.string.consent_privacy_rights))
        }
        if (prompt.confirmation) {
            append(
                context.getString(R.string.consent_confirmation)
                    .format(getModalitySpecificUseCaseText(context))
            )
        }
    }

    private fun getModalitySpecificUseCaseText(context: Context) = if (isSingleModality()) {
        getSingleModalitySpecificUseCaseText(context)
    } else {
        getConcatenatedModalitiesUseCaseText(context)
    }

    private fun getConcatenatedModalitiesUseCaseText(context: Context) =
        String.format(
            "%s %s %s", context.getString(R.string.biometrics_general_fingerprint),
            context.getString(R.string.biometric_concat_modalities),
            context.getString(R.string.biometric_general_face)
        )

    private fun getSingleModalitySpecificUseCaseText(context: Context) =
        when (modalities.first()) {
            GeneralConfiguration.Modality.FACE -> context.getString(R.string.biometric_general_face)
            GeneralConfiguration.Modality.FINGERPRINT -> context.getString(R.string.biometrics_general_fingerprint)
        }

    private fun getModalitySpecificAccessText(context: Context) = if (isSingleModality()) {
        getSingleModalityAccessText(context)
    } else {
        getConcatenatedModalitiesAccessText(context)
    }

    private fun getConcatenatedModalitiesAccessText(context: Context) =
        context.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText(context: Context) = when (modalities.first()) {
        GeneralConfiguration.Modality.FACE -> context.getString(R.string.biometrics_access_face)
        GeneralConfiguration.Modality.FINGERPRINT -> context.getString(R.string.biometrics_access_fingerprint)
    }

    private fun isSingleModality() = modalities.size == 1
}
