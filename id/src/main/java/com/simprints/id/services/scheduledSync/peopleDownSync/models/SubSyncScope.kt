package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.Constants

data class SubSyncScope(val projectId: String, val userId: String?, val moduleId: String?) {
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleId ?: "_"}"

    val group: Constants.GROUP
        get() = when {
            moduleId != null -> Constants.GROUP.MODULE
            userId != null -> Constants.GROUP.USER
            else -> Constants.GROUP.GLOBAL
        }
}
