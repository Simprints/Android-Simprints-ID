package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.TokenKeyType

internal fun Project.toProto(): ProtoProject = ProtoProject
    .newBuilder()
    .setId(id)
    .setCreator(creator)
    .setDescription(description)
    .setName(name)
    .setState(state.name)
    .setImageBucket(imageBucket)
    .also {
        if (baseUrl != null) it.baseUrl = baseUrl
        it.putAllTokenizationKeys(tokenizationKeys.mapTokenizationKeysToLocal())
    }.build()

internal fun ProtoProject.toDomain(): Project {
    val tokenizationKeys = tokenizationKeysMap.mapTokenizationKeysToDomain()
    return Project(
        id = id,
        name = name,
        // We assume the project is RUNNING, otherwise it would have cleared the data
        state = state
            ?.ifBlank { null }
            ?.let { ProjectState.valueOf(it) }
            ?: ProjectState.RUNNING,
        description = description,
        creator = creator,
        imageBucket = imageBucket,
        baseUrl = baseUrl,
        tokenizationKeys = tokenizationKeys,
    )
}

internal fun Map<String, String>?.mapTokenizationKeysToDomain(): Map<TokenKeyType, String> = (this ?: emptyMap()).mapKeys { entry ->
    runCatching { TokenKeyType.valueOf(entry.key) }.getOrElse { TokenKeyType.Unknown }
}

internal fun Map<TokenKeyType, String>.mapTokenizationKeysToLocal(): Map<String, String> = mapKeys { entry ->
    entry.key.toString()
}
