package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.Constants

data class SyncScope(val projectId: String,
                     val userId: String?, //StopShip - Discuss: Domain and Real Classes save userId as ""
                     val moduleIds: Set<String>?) {

    val uniqueKey: String = "${projectId}_${userId ?: ""}${moduleIds?.fold("") { acc, s -> "${acc}_$s"} ?: "_"}"

    fun toSubSyncScopes(): List<SubSyncScope> = moduleIds?.let { moduleIds ->
        if (moduleIds.isEmpty()) {
            return listOf(SubSyncScope(projectId, userId, null))
        }

        moduleIds.map { moduleId ->
            SubSyncScope(projectId, userId, moduleId)
        }
    } ?: listOf(SubSyncScope(projectId, userId, null))

    val group: Constants.GROUP
        get() = when {
            moduleIds != null -> Constants.GROUP.MODULE
            userId != null -> Constants.GROUP.USER
            else -> Constants.GROUP.GLOBAL
        }
}
