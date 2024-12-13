package com.simprints.infra.config.store.models

data class ConsentConfiguration(
    val programName: String,
    val organizationName: String,
    val collectConsent: Boolean,
    val displaySimprintsLogo: Boolean,
    val allowParentalConsent: Boolean,
    val generalPrompt: ConsentPromptConfiguration?,
    val parentalPrompt: ConsentPromptConfiguration?,
) {
    data class ConsentPromptConfiguration(
        val enrolmentVariant: ConsentEnrolmentVariant,
        val dataSharedWithPartner: Boolean,
        val dataUsedForRAndD: Boolean,
        val privacyRights: Boolean,
        val confirmation: Boolean,
    )

    enum class ConsentEnrolmentVariant {
        STANDARD,
        ENROLMENT_ONLY,
    }
}
