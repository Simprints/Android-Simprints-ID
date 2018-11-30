package com.simprints.id.services.scheduledSync.peopleDownSync.newplan

data class SyncScope(val projectId: String, val userId: String?, val moduleIds: Set<String>?) {

    fun toSubSyncScopes(): List<SubSyncScope> = moduleIds?.let {  moduleIds ->
        moduleIds.map { moduleId ->
            SubSyncScope(projectId, userId, moduleId)
        }
    } ?: listOf(SubSyncScope(projectId, userId, null))
}
