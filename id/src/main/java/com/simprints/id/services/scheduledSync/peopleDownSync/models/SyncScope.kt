package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.Constants

data class SyncScope(val group: Constants.GROUP,
                     val projectId: String,
                     val userId: String?,
                     val moduleIds: Set<String>?) {

    val uniqueKey: String = "${projectId}_${userId ?: ""}${moduleIds?.fold("") { acc, s -> "${acc}_$s"} ?: "_"}"

    fun toSubSyncScopes(): List<SubSyncScope> = moduleIds?.let { moduleIds ->
        moduleIds.map { moduleId ->
            SubSyncScope(group, projectId, userId, moduleId)
        }
    } ?: listOf(SubSyncScope(group, projectId, userId, null))
}
