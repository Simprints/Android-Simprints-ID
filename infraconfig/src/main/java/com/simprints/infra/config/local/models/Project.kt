package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.Project

internal fun Project.toProto(): ProtoProject =
    ProtoProject.newBuilder()
        .setId(id)
        .setCreator(creator)
        .setDescription(description)
        .setName(name)
        .setImageBucket(imageBucket)
        .also {
            if (baseUrl != null) it.baseUrl = baseUrl
        }
        .build()

internal fun ProtoProject.toDomain(): Project =
    Project(id, name, description, creator, imageBucket, baseUrl)
