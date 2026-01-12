package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.local.models.mapTokenizationKeysToDomain
import com.simprints.infra.config.store.models.Project
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiProject(
    val id: String,
    val name: String,
    val state: ApiProjectState,
    val description: String,
    val creator: String,
    val imageBucket: String,
    val baseUrl: String? = null,
    val configuration: ApiProjectConfiguration,
    val tokenizationKeys: Map<String, String>? = null,
) {
    fun toDomain(): Project = Project(
        id = id,
        name = name,
        state = state.toDomain(),
        description = description,
        creator = creator,
        imageBucket = imageBucket,
        baseUrl = baseUrl,
        tokenizationKeys = tokenizationKeys.mapTokenizationKeysToDomain(),
    )
}
