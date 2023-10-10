package com.simprints.feature.consent.screens.consent.helpers

import android.content.Context
import com.simprints.feature.consent.ConsentType
import com.simprints.infra.config.store.models.ConsentConfiguration
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.resources.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GeneralConsentTextHelper @Inject constructor(
    @ApplicationContext val context: Context,
) {
    // TODO All the `getString(id).format(arg,arg)` calls should be `getString(id,arg,arg)` one strings are fixed

    //First argument in consent text should always be program name, second is modality specific access/use case text
    fun assembleText(
        config: ConsentConfiguration,
        modalities: List<GeneralConfiguration.Modality>,
        consentType: ConsentType,
    ) = StringBuilder().apply {
        val modalityUseCase = getModalitySpecificUseCaseText(modalities)
        val modalityAccess = getModalitySpecificAccessText(modalities)

        filterAppRequestForConsent(consentType, config, modalityUseCase)
        filterForDataSharingOptions(config, modalityUseCase, modalityAccess)
    }.toString()

    private fun StringBuilder.filterAppRequestForConsent(
        consentType: ConsentType,
        config: ConsentConfiguration,
        modalityUseCase: String,
    ) {
        when (consentType) {
            ConsentType.ENROL -> appendTextForConsentEnrol(config.generalPrompt, config.programName, modalityUseCase)
            ConsentType.IDENTIFY, ConsentType.VERIFY -> appendTextForConsentVerifyOrIdentify(config.programName, modalityUseCase)
        }
    }

    private fun StringBuilder.appendTextForConsentEnrol(
        config: ConsentConfiguration.ConsentPromptConfiguration?,
        programName: String,
        modalityUseCase: String,
    ) = when (config?.enrolmentVariant) {
        ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY -> append(
            context.getString(R.string.consent_enrol_only).format(programName, modalityUseCase)
        )

        ConsentConfiguration.ConsentEnrolmentVariant.STANDARD -> append(
            context.getString(R.string.consent_enrol).format(programName, modalityUseCase)
        )

        else -> this
    }

    private fun StringBuilder.appendTextForConsentVerifyOrIdentify(programName: String, modalityUseCase: String) = append(
        context.getString(R.string.consent_id_verify).format(programName, modalityUseCase)
    )

    private fun StringBuilder.filterForDataSharingOptions(
        config: ConsentConfiguration,
        modalityUseCase: String,
        modalityAccess: String,
    ) {
        if (config.generalPrompt?.dataSharedWithPartner == true) {
            append(
                context.getString(R.string.consent_share_data_yes)
                    .format(config.organizationName, modalityAccess)
            )
        } else {
            append(
                context.getString(R.string.consent_share_data_no)
                    .format(modalityAccess)
            )
        }
        if (config.generalPrompt?.dataUsedForRAndD == true) {
            append(context.getString(R.string.consent_collect_yes))
        }
        if (config.generalPrompt?.privacyRights == true) {
            append(context.getString(R.string.consent_privacy_rights))
        }
        if (config.generalPrompt?.confirmation == true) {
            append(
                context.getString(R.string.consent_confirmation).format(modalityUseCase)
            )
        }
    }

    private fun getModalitySpecificUseCaseText(modalities: List<GeneralConfiguration.Modality>) = if (modalities.size == 1) {
        getSingleModalitySpecificUseCaseText(modalities)
    } else {
        getConcatenatedModalitiesUseCaseText()
    }

    private fun getConcatenatedModalitiesUseCaseText() = "%s %s %s".format(
        context.getString(R.string.biometrics_general_fingerprint),
        context.getString(R.string.biometric_concat_modalities),
        context.getString(R.string.biometric_general_face)
    )

    private fun getSingleModalitySpecificUseCaseText(modalities: List<GeneralConfiguration.Modality>) = when (modalities.first()) {
        GeneralConfiguration.Modality.FACE -> context.getString(R.string.biometric_general_face)
        GeneralConfiguration.Modality.FINGERPRINT -> context.getString(R.string.biometrics_general_fingerprint)
    }

    private fun getModalitySpecificAccessText(modalities: List<GeneralConfiguration.Modality>) = if (modalities.size == 1) {
        getSingleModalityAccessText(modalities)
    } else {
        getConcatenatedModalitiesAccessText()
    }

    private fun getConcatenatedModalitiesAccessText() = context.getString(R.string.biometrics_access_fingerprint_face)

    private fun getSingleModalityAccessText(modalities: List<GeneralConfiguration.Modality>) = when (modalities.first()) {
        GeneralConfiguration.Modality.FACE -> context.getString(R.string.biometrics_access_face)
        GeneralConfiguration.Modality.FINGERPRINT -> context.getString(R.string.biometrics_access_fingerprint)
    }

}
