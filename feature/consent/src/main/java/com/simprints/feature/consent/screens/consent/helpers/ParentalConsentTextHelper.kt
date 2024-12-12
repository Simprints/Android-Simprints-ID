package com.simprints.feature.consent.screens.consent.helpers

import android.content.Context
import com.simprints.feature.consent.ConsentType
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.resources.R

internal data class ParentalConsentTextHelper(
    private val config: ConsentConfiguration,
    private val modalities: List<Modality>,
    private val consentType: ConsentType,
) {
    // TODO All the `getString(id).format(arg,arg)` calls should be `getString(id,arg,arg)` one strings are fixed

    // First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(context: Context): String = StringBuilder()
        .apply {
            val modalityUseCase = getModalitySpecificUseCaseText(context, modalities)
            val modalityAccess = getModalitySpecificAccessText(context, modalities)

            filterAppRequestForParentalConsent(context, consentType, config, modalityUseCase)
            extractDataSharingOptions(context, config, modalityUseCase, modalityAccess)
        }.toString()

    private fun StringBuilder.filterAppRequestForParentalConsent(
        context: Context,
        consentType: ConsentType,
        config: ConsentConfiguration,
        modalityUseCase: String,
    ) {
        when (consentType) {
            ConsentType.ENROL -> appendTextForParentalEnrol(context, config.parentalPrompt, config.programName, modalityUseCase)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForIdentifyOrVerify(context, config.programName, modalityUseCase)
        }
    }

    private fun StringBuilder.appendTextForParentalEnrol(
        context: Context,
        config: ConsentConfiguration.ConsentPromptConfiguration?,
        programName: String,
        modalityUseCase: String,
    ) = when (config?.enrolmentVariant) {
        ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY -> appendSentence(
            context
                .getString(R.string.consent_parental_enrol_only)
                .format(programName, modalityUseCase),
        )

        ConsentConfiguration.ConsentEnrolmentVariant.STANDARD -> appendSentence(
            context
                .getString(R.string.consent_parental_enrol)
                .format(programName, modalityUseCase),
        )

        else -> this
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify(
        context: Context,
        programName: String,
        modalityUseCase: String,
    ) = appendSentence(
        context.getString(R.string.consent_parental_id_verify).format(programName, modalityUseCase),
    )

    private fun StringBuilder.extractDataSharingOptions(
        context: Context,
        config: ConsentConfiguration,
        modalityUseCase: String,
        modalityAccess: String,
    ) {
        if (config.parentalPrompt?.dataSharedWithPartner == true) {
            appendSentence(
                context.getString(R.string.consent_parental_share_data_yes).format(config.organizationName, modalityAccess),
            )
        } else {
            appendSentence(
                context.getString(R.string.consent_parental_share_data_no).format(modalityAccess),
            )
        }
        if (config.parentalPrompt?.dataUsedForRAndD == true) {
            appendSentence(context.getString(R.string.consent_collect_yes))
        }
        if (config.parentalPrompt?.privacyRights == true) {
            appendSentence(context.getString(R.string.consent_parental_privacy_rights))
        }
        if (config.parentalPrompt?.confirmation == true) {
            appendSentence(
                context.getString(R.string.consent_parental_confirmation).format(modalityUseCase),
            )
        }
    }

    private fun getModalitySpecificUseCaseText(
        context: Context,
        modalities: List<Modality>,
    ) = if (modalities.size == 1) {
        getSingleModalitySpecificUseCaseText(context, modalities)
    } else {
        getConcatenatedModalitiesUseCaseText(context)
    }

    private fun getConcatenatedModalitiesUseCaseText(context: Context) = "%s %s %s".format(
        context.getString(R.string.consent_biometrics_parental_fingerprint),
        context.getString(R.string.consent_biometric_concat_modalities),
        context.getString(R.string.consent_biometrics_parental_face),
    )

    private fun getSingleModalitySpecificUseCaseText(
        context: Context,
        modalities: List<Modality>,
    ) = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.consent_biometrics_parental_face)
        Modality.FINGERPRINT -> context.getString(R.string.consent_biometrics_parental_fingerprint)
        else -> ""
    }

    private fun getModalitySpecificAccessText(
        context: Context,
        modalities: List<Modality>,
    ) = if (modalities.size == 1) {
        getSingleModalityAccessText(context, modalities)
    } else {
        getConcatenatedModalitiesAccessText(context)
    }

    private fun getConcatenatedModalitiesAccessText(context: Context) =
        context.getString(R.string.consent_biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText(
        context: Context,
        modalities: List<Modality>,
    ) = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.consent_biometrics_access_face)
        Modality.FINGERPRINT -> context.getString(R.string.consent_biometrics_access_fingerprint)
        else -> ""
    }
}
