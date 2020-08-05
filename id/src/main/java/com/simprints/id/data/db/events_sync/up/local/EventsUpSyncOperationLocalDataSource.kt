package com.simprints.id.data.db.events_sync.up.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventsUpSyncOperationLocalDataSource {

    @Query("select * from DbEventsUpSyncOperation")
    suspend fun load(): List<DbEventsUpSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(operation: DbEventsUpSyncOperation)

    @Query("delete from DbEventsUpSyncOperation")
    suspend fun deleteAll()
}
