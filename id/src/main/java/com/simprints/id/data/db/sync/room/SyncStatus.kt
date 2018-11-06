package com.simprints.id.data.db.sync.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "SyncStatus")
data class SyncStatus(
    @PrimaryKey val id: String = SYNC_STATUS_CONST_ID,
    var peopleToUpSync: Int = 0,
    var lastUpSyncTime: String? = null,
    var peopleToDownSync: Int = 0,
    var lastDownSyncTime: String? = null
    ) {
    companion object {
        const val SYNC_STATUS_CONST_ID = "CONSTANT_IDENTIFIER_FOR_SYNC_STATUS"
    }
}
