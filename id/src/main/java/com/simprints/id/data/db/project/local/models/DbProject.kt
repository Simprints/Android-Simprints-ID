package com.simprints.id.data.db.project.local.models

import com.simprints.id.data.db.project.domain.Project
import com.simprints.infra.realm.models.DbProject

fun DbProject.fromDbToDomain(): Project = Project(
    id,
    name,
    description,
    creator,
    imageBucket,
    if (updatedAt.isBlank()) null else updatedAt
)

fun Project.fromDomainToDb(): DbProject = DbProject().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name
    it.imageBucket = imageBucket
    it.updatedAt = updatedAt ?: ""
}
