package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.GROUP

data class SyncScope(val projectId: String,
                     val userId: String?, //TODO - Discuss: Domain and Real Classes save userId as ""
                     val moduleIds: Set<String>?) {

    val uniqueKey: String = "${projectId}_${userId ?: ""}${moduleIds?.fold("") { acc, s -> "${acc}_$s"} ?: "_"}"

    fun toSubSyncScopes(): List<SubSyncScope> =
        moduleIds?.let { moduleIds ->
            moduleIds.map { moduleId ->
                SubSyncScope(projectId, userId, moduleId)
            }
        } ?: listOf(SubSyncScope(projectId, userId, null))

    val group: GROUP
        get() = when {
            moduleIds != null -> GROUP.MODULE
            userId != null -> GROUP.USER
            else -> GROUP.GLOBAL
        }
}
