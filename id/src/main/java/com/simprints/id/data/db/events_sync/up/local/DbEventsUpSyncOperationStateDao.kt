package com.simprints.id.data.db.events_sync.up.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DbEventsUpSyncOperationStateDao {

    @Query("select * from DbEventsUpSyncOperation")
    suspend fun load(): List<DbEventsUpSyncOperationState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(operation: DbEventsUpSyncOperationState)

    @Query("delete from DbEventsUpSyncOperation")
    suspend fun deleteAll()
}
