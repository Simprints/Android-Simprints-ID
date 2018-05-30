package com.simprints.id.data.db.local.realm.models.adapters

import com.simprints.id.data.db.local.realm.models.rl_Project
import com.simprints.id.domain.Project


fun rl_Project.toProject(): Project = Project().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name

    it.legacyId =
        if (legacyId.isBlank())
            null
        else legacyId

    it.updatedAt =
        if (updatedAt.isBlank())
            null
        else updatedAt
}

fun Project.toRealmProject(): rl_Project = rl_Project().also {
    it.id = id
    it.creator = creator
    it.description = description
    it.name = name
    it.legacyId = legacyId ?: ""
    it.updatedAt = updatedAt ?: ""
}
