package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.local.models.mapTokenizationKeysToDomain

@Keep
internal data class ApiProject(
    val id: String,
    val name: String,
    val description: String,
    val creator: String,
    val imageBucket: String,
    val baseUrl: String?,
    val tokenizationKeys: Map<String, String>?
) {
    fun toDomain(): Project {
        return Project(
            id = id,
            name = name,
            description = description,
            creator = creator,
            imageBucket = imageBucket,
            baseUrl = baseUrl,
            tokenizationKeys = tokenizationKeys.mapTokenizationKeysToDomain()
        )
    }
}
