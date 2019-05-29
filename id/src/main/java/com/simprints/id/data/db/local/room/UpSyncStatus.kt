package com.simprints.id.data.db.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.Keep

@Entity(tableName = "UpSyncStatus")
@Keep
data class UpSyncStatus(
    @PrimaryKey val upSyncStatusId: String = UP_SYNC_STATUS_CONST_ID,
    val lastUpSyncTime: Long? = null
) {
    companion object {
        const val UP_SYNC_STATUS_CONST_ID = "up_sync_status_const_id"
    }
}
