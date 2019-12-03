package com.simprints.id.services.scheduledSync.peopleDownSync.models

import androidx.annotation.Keep
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes

@Keep
data class SubSyncScope(val projectId: String,
                        val userId: String?,
                        val moduleId: String?,
                        val modes: List<Modes>) {
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleId ?: "_"}_${modes.joinToString("_")}"

    val group: GROUP
        get() = when {
            moduleId != null -> GROUP.MODULE
            userId != null -> GROUP.USER
            else -> GROUP.GLOBAL
        }
}
