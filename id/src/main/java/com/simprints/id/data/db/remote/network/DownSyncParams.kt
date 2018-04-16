package com.simprints.id.data.db.remote.network

import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.exceptions.safe.data.db.NoStoredLastSyncedInfo
import com.simprints.id.services.sync.SyncTaskParameters
import java.util.*

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
class DownSyncParams(syncParams: SyncTaskParameters,
                     localDbManager: LocalDbManager) : HashMap<String, Any>() {

    companion object {
        const val LAST_KNOWN_PATIENT_ID = "lastKnownPatientId"
        const val LAST_KNOWN_PATIENT_AT = "lastKnownPatientUpdatedAt"
        const val BATCH_SIZE = "batchSize"
        const val BATCH_SIZE_VALUE = 5000
    }

    init {
        syncParams.userId?.let { this[SyncTaskParameters.USER_ID_FIELD] = it }
        syncParams.moduleId?.let { this[SyncTaskParameters.MODULE_ID_FIELD] = it }

        try {
            localDbManager
                .getSyncInfoFor(syncParams.toGroup())
                .blockingGet().let {
                    this[LAST_KNOWN_PATIENT_AT] = it.lastKnownPatientUpdatedAt.time
                    if (it.lastKnownPatientId.isNotEmpty())
                        this[LAST_KNOWN_PATIENT_ID] = it.lastKnownPatientId
                }
        } catch (e: NoStoredLastSyncedInfo) {
        }

        this[BATCH_SIZE] = BATCH_SIZE_VALUE
    }
}
