package com.simprints.id.data.db.remote.network

import com.simprints.id.data.db.local.room.DownSyncDao
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import java.io.Serializable
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
class DownSyncParams(subSyncScope: SubSyncScope,
                     specificModule: String? = null,
                     downSyncDao: DownSyncDao) : Serializable {

    val projectId: String = subSyncScope.projectId
    val userId: String? = subSyncScope.userId
    val moduleId: String? = specificModule

    var lastKnownPatientId: String? = null
    var lastKnownPatientUpdatedAt: Long? = null

    init {
        try {
            val downSyncStatus = downSyncDao.getDownSyncStatusForId(downSyncDao.getStatusId(subSyncScope))
            downSyncStatus?.lastPatientUpdatedAt?.let {
                lastKnownPatientUpdatedAt = Date(it).time
            }

            downSyncStatus?.lastPatientId?.let {
                lastKnownPatientId = it
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
