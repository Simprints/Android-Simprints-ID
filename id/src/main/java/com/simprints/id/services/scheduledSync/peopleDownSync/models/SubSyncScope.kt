package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.GROUP

data class SubSyncScope(val projectId: String, val userId: String?, val moduleId: String?) {
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleId ?: "_"}"

    val group: GROUP
        get() = when {
            moduleId != null -> GROUP.MODULE
            userId != null -> GROUP.USER
            else -> GROUP.GLOBAL
        }
}
