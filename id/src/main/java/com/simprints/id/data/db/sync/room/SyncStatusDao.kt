package com.simprints.id.data.db.sync.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.simprints.id.data.db.sync.room.SyncStatus.Companion.SYNC_STATUS_CONST_ID

@Dao
interface SyncStatusDao {

    @Query("select * from SyncStatus where id = '$SYNC_STATUS_CONST_ID'")
    fun getSyncStatus(): LiveData<SyncStatus>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDefaultSyncStatus(syncStatus: SyncStatus)

    @Query("update SyncStatus set peopleToUpSync = :peopleToUpSync where id = '$SYNC_STATUS_CONST_ID'")
    fun updatePeopleToUpSyncCount(peopleToUpSync: Int)

    @Query("update SyncStatus set peopleToDownSync = :peopleToDownSync where id = '$SYNC_STATUS_CONST_ID'")
    fun updatePeopleToDownSyncCount(peopleToDownSync: Int)

    @Query("update SyncStatus set lastUpSyncTime = :lastUpSyncTime where id = '$SYNC_STATUS_CONST_ID'")
    fun updateLastUpSyncTime(lastUpSyncTime: String)

    @Query("update SyncStatus set lastDownSyncTime = :lastDownSyncTime where id= '$SYNC_STATUS_CONST_ID'")
    fun updateLastDownSyncTime(lastDownSyncTime: String)
}
