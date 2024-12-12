package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.ConsentConfiguration

@Keep
internal data class ApiConsentConfiguration(
    val programName: String,
    val organizationName: String,
    val collectConsent: Boolean,
    val displaySimprintsLogo: Boolean,
    val allowParentalConsent: Boolean,
    val generalPrompt: ConsentPromptConfiguration?,
    val parentalPrompt: ConsentPromptConfiguration?,
) {
    fun toDomain(): ConsentConfiguration = ConsentConfiguration(
        programName,
        organizationName,
        collectConsent,
        displaySimprintsLogo,
        allowParentalConsent,
        generalPrompt?.toDomain(),
        parentalPrompt?.toDomain(),
    )

    @Keep
    data class ConsentPromptConfiguration(
        val enrolmentVariant: ConsentEnrolmentVariant,
        val dataSharedWithPartner: Boolean,
        val dataUsedForRAndD: Boolean,
        val privacyRights: Boolean,
        val confirmation: Boolean,
    ) {
        fun toDomain(): ConsentConfiguration.ConsentPromptConfiguration = ConsentConfiguration.ConsentPromptConfiguration(
            enrolmentVariant.toDomain(),
            dataSharedWithPartner,
            dataUsedForRAndD,
            privacyRights,
            confirmation,
        )
    }

    @Keep
    enum class ConsentEnrolmentVariant {
        STANDARD,
        ENROLMENT_ONLY,
        ;

        fun toDomain(): ConsentConfiguration.ConsentEnrolmentVariant = when (this) {
            STANDARD -> ConsentConfiguration.ConsentEnrolmentVariant.STANDARD
            ENROLMENT_ONLY -> ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
        }
    }
}
