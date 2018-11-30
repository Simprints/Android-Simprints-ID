package com.simprints.id.data.db.remote.network

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.exceptions.safe.data.db.NoStoredLastSyncedInfoException
import com.simprints.id.services.sync.SyncTaskParameters

/**
 * An hashmap with (all optionals):
 * moduleId
 * userId
 * lastKnownPatientId
 * lastKnownPatientUpdatedAt
 * batchSize
 *
 * Based on syncParams and last updated user
 */
class DownSyncParams(private val syncParams: SyncTaskParameters,
                     specificModule: String? = null,
                     localDbManager: LocalDbManager) {

    val projectId: String = syncParams.projectId
    val userId: String? = syncParams.userId
    val moduleId: String? = specificModule

    var lastKnownPatientId: String? = null
    var lastKnownPatientUpdatedAt: Long? = null

    init {
        try {
            localDbManager
                .getSyncInfoFor(syncParams.toGroup(), moduleId)
                .blockingGet().let {
                    lastKnownPatientUpdatedAt = it.lastKnownPatientUpdatedAt.time
                    if (it.lastKnownPatientId.isNotEmpty())
                        lastKnownPatientId = it.lastKnownPatientId
                }
        } catch (e: NoStoredLastSyncedInfoException) {
        }
    }

    fun toGroup() = syncParams.toGroup()
}

fun SyncTaskParameters.toDownSyncParams(localDbManager: LocalDbManager): List<DownSyncParams> =
    this.moduleIds?.let { moduleIds ->
        moduleIds.map { moduleId ->
            DownSyncParams(this, moduleId, localDbManager)
        }
    } ?: listOf(DownSyncParams(this, null, localDbManager))
