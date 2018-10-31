package com.simprints.id.data.db.sync.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
data class SyncStatus(
    @PrimaryKey val id: String = SYNC_STATUS_CONST_ID,
    var peopleToUpSync: Int = 0,
    var lastUpSyncTime: Date? = null,
    var peopleToDownSync: Int = 0,
    var lastDownSyncTime: Date? = null
    ) {
    companion object {
        const val SYNC_STATUS_CONST_ID = "Constant-Identifier-For-Sync-Status"
    }
}
