package com.simprints.id.services.scheduledSync.peopleDownSync.models

import com.simprints.id.domain.Constants

data class SubSyncScope(val group: Constants.GROUP, val projectId: String, val userId: String?, val moduleId: String?) {
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleId ?: "_"}"
}
