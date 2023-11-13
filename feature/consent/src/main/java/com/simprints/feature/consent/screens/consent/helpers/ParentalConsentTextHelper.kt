package com.simprints.feature.consent.screens.consent.helpers

import android.content.Context
import com.simprints.feature.consent.ConsentType
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration.Modality
import com.simprints.infra.resources.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ParentalConsentTextHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    // TODO All the `getString(id).format(arg,arg)` calls should be `getString(id,arg,arg)` one strings are fixed

    //First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(
        config: ConsentConfiguration,
        modalities: List<Modality>,
        consentType: ConsentType,
    ): String = StringBuilder().apply {
        val modalityUseCase = getModalitySpecificUseCaseText(modalities)
        val modalityAccess = getModalitySpecificAccessText(modalities)

        filterAppRequestForParentalConsent(consentType, config, modalityUseCase)
        extractDataSharingOptions(config, modalityUseCase, modalityAccess)
    }.toString()

    private fun StringBuilder.filterAppRequestForParentalConsent(
        consentType: ConsentType,
        config: ConsentConfiguration,
        modalityUseCase: String,
    ) {
        when (consentType) {
            ConsentType.ENROL -> appendTextForParentalEnrol(config.parentalPrompt, config.programName, modalityUseCase)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForIdentifyOrVerify(config.programName, modalityUseCase)
        }
    }

    private fun StringBuilder.appendTextForParentalEnrol(
        config: ConsentConfiguration.ConsentPromptConfiguration?,
        programName: String,
        modalityUseCase: String,
    ) = when (config?.enrolmentVariant) {
        ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY -> append(
            context.getString(R.string.consent_parental_enrol_only)
                .format(programName, modalityUseCase)
        )

        ConsentConfiguration.ConsentEnrolmentVariant.STANDARD -> append(
            context.getString(R.string.consent_parental_enrol)
                .format(programName, modalityUseCase)
        )

        else -> this
    }

    private fun StringBuilder.appendTextForIdentifyOrVerify(programName: String, modalityUseCase: String) = append(
        context.getString(R.string.consent_parental_id_verify).format(programName, modalityUseCase)
    )

    private fun StringBuilder.extractDataSharingOptions(
        config: ConsentConfiguration,
        modalityUseCase: String,
        modalityAccess: String
    ) {
        if (config.parentalPrompt?.dataSharedWithPartner == true) {
            append(
                context.getString(R.string.consent_parental_share_data_yes).format(config.organizationName, modalityAccess)
            )
        } else {
            append(
                context.getString(R.string.consent_parental_share_data_no).format(modalityAccess)
            )
        }
        if (config.parentalPrompt?.dataUsedForRAndD == true) {
            append(context.getString(R.string.consent_collect_yes))
        }
        if (config.parentalPrompt?.privacyRights == true) {
            append(context.getString(R.string.consent_parental_privacy_rights))
        }
        if (config.parentalPrompt?.confirmation == true) {
            append(
                context.getString(R.string.consent_parental_confirmation).format(modalityUseCase)
            )
        }
    }

    private fun getModalitySpecificUseCaseText(modalities: List<Modality>) = if (modalities.size == 1) {
        getSingleModalitySpecificUseCaseText(modalities)
    } else {
        getConcatenatedModalitiesUseCaseText()
    }

    private fun getConcatenatedModalitiesUseCaseText() = "%s %s %s".format(
        context.getString(R.string.biometrics_parental_fingerprint),
        context.getString(R.string.biometric_concat_modalities),
        context.getString(R.string.biometrics_parental_face)
    )

    private fun getSingleModalitySpecificUseCaseText(modalities: List<Modality>) = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.biometrics_parental_face)
        Modality.FINGERPRINT -> context.getString(R.string.biometrics_parental_fingerprint)
        else -> ""
    }

    private fun getModalitySpecificAccessText(modalities: List<Modality>) = if (modalities.size == 1) {
        getSingleModalityAccessText(modalities)
    } else {
        getConcatenatedModalitiesAccessText()
    }

    private fun getConcatenatedModalitiesAccessText() = context.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText(modalities: List<Modality>) = when (modalities.first()) {
        Modality.FACE -> context.getString(R.string.biometrics_access_face)
        Modality.FINGERPRINT -> context.getString(R.string.biometrics_access_fingerprint)
        else -> ""
    }
}
