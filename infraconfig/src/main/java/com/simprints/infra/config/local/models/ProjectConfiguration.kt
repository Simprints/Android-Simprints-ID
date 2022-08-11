package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.ConsentConfiguration
import com.simprints.infra.config.domain.models.ProjectConfiguration

internal fun ProjectConfiguration.toProto(): ProtoProjectConfiguration =
    ProtoProjectConfiguration.newBuilder()
        .setProjectId(projectId)
        .setConsent(consent.toProto())
        .build()


