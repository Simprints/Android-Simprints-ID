package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.Project

@Keep
internal data class ApiProject(
    val id: String,
    val name: String,
    val description: String,
    val creator: String,
    val imageBucket: String,
) {
    fun toDomain(): Project = Project(id, name, description, creator, imageBucket)
}
