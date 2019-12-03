package com.simprints.id.services.scheduledSync.peopleDownSync.models

import androidx.annotation.Keep
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modes

@Keep
data class SyncScope(val projectId: String,
                     val userId: String?, //TODO - Discuss: Domain and Real Classes save userId as ""
                     val moduleIds: Set<String>?,
                     val modes: List<Modes> = listOf(Modes.FINGERPRINT)) {

    //StopShip: do we need migration in room?
    val uniqueKey: String = "${projectId}_${userId ?: ""}_${moduleIds?.joinToString("_")}}"

    fun toSubSyncScopes(): List<SubSyncScope> =
        moduleIds?.let { moduleIds ->
            moduleIds.map { moduleId ->
                SubSyncScope(projectId, userId, moduleId, modes)
            }
        } ?: listOf(SubSyncScope(projectId, userId, null, modes))

    val group: GROUP
        get() = when {
            moduleIds != null -> GROUP.MODULE
            userId != null -> GROUP.USER
            else -> GROUP.GLOBAL
        }
}
