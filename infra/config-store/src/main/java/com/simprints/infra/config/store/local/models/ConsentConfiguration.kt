package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.ConsentConfiguration

internal fun ConsentConfiguration.toProto(): ProtoConsentConfiguration = ProtoConsentConfiguration
    .newBuilder()
    .setProgramName(programName)
    .setOrganizationName(organizationName)
    .setCollectConsent(collectConsent)
    .setDisplaySimprintsLogo(displaySimprintsLogo)
    .setAllowParentalConsent(allowParentalConsent)
    .also {
        if (generalPrompt != null) it.generalPrompt = generalPrompt.toProto()
        if (parentalPrompt != null) it.parentalPrompt = parentalPrompt.toProto()
    }.build()

internal fun ConsentConfiguration.ConsentPromptConfiguration.toProto(): ProtoConsentConfiguration.ConsentPromptConfiguration =
    ProtoConsentConfiguration.ConsentPromptConfiguration
        .newBuilder()
        .setEnrolmentVariant(enrolmentVariant.toProto())
        .setDataSharedWithPartner(dataSharedWithPartner)
        .setDataUsedForRAndD(dataUsedForRAndD)
        .setPrivacyRights(privacyRights)
        .setConfirmation(confirmation)
        .build()

internal fun ConsentConfiguration.ConsentEnrolmentVariant.toProto(): ProtoConsentConfiguration.ConsentEnrolmentVariant = when (this) {
    ConsentConfiguration.ConsentEnrolmentVariant.STANDARD -> ProtoConsentConfiguration.ConsentEnrolmentVariant.STANDARD
    ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY -> ProtoConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
}

internal fun ProtoConsentConfiguration.toDomain(): ConsentConfiguration = ConsentConfiguration(
    programName,
    organizationName,
    collectConsent,
    displaySimprintsLogo,
    allowParentalConsent,
    hasGeneralPrompt().let { if (it) generalPrompt.toDomain() else null },
    hasParentalPrompt().let { if (it) parentalPrompt.toDomain() else null },
)

internal fun ProtoConsentConfiguration.ConsentPromptConfiguration.toDomain(): ConsentConfiguration.ConsentPromptConfiguration =
    ConsentConfiguration.ConsentPromptConfiguration(
        enrolmentVariant.toDomain(),
        dataSharedWithPartner,
        dataUsedForRAndD,
        privacyRights,
        confirmation,
    )

internal fun ProtoConsentConfiguration.ConsentEnrolmentVariant.toDomain(): ConsentConfiguration.ConsentEnrolmentVariant = when (this) {
    ProtoConsentConfiguration.ConsentEnrolmentVariant.STANDARD -> ConsentConfiguration.ConsentEnrolmentVariant.STANDARD
    ProtoConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY -> ConsentConfiguration.ConsentEnrolmentVariant.ENROLMENT_ONLY
    ProtoConsentConfiguration.ConsentEnrolmentVariant.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid ConsentEnrolmentVariant $name",
    )
}
