package com.simprints.id.data.db.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Aashay - UpSyncStatus: room entity
 *  1) LastSyncTime: Long?
 */
@Entity(tableName = "UpSyncStatus")
data class UpSyncStatus(
    @PrimaryKey val upSyncStatusId: String = UP_SYNC_STATUS_CONST_ID,
    val lastUpSyncTime: Long? = null
) {
    companion object {
        const val UP_SYNC_STATUS_CONST_ID = "up_sync_status_const_id"
    }
}
