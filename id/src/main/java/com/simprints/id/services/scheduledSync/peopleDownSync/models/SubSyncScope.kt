package com.simprints.id.services.scheduledSync.peopleDownSync.models

data class SubSyncScope(val projectId: String, val userId: String?, val moduleId: String?) {
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleId?: "_"}"
}
