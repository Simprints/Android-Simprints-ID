package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.ConsentConfiguration

internal fun ConsentConfiguration.toProto(): ProtoConsentConfiguration =
    ProtoConsentConfiguration.newBuilder()
        .setProgramName(programName)
        .setOrganizationName(organizationName)
        .setCollectConsent(collectConsent)
        .setDisplaySimprintsLogo(displaySimprintsLogo)
        .setAllowParentalConsent(allowParentalConsent)
        .also {
            if (generalPrompt != null) it.generalPrompt = generalPrompt.toProto()
            if (parentalPrompt != null) it.parentalPrompt = parentalPrompt.toProto()
        }
        .build()

internal fun ConsentConfiguration.ConsentPromptConfiguration.toProto(): ProtoConsentConfiguration.ConsentPromptConfiguration =
    ProtoConsentConfiguration.ConsentPromptConfiguration.newBuilder()

        .build()
