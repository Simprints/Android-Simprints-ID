package com.simprints.id.data.db.events_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventDownSyncOperationLocalDataSource {


    @Query("select * from DbEventsDownSyncOperation")
    suspend fun load(): List<DbEventsDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEventsDownSyncOperation: DbEventsDownSyncOperation)

    @Query("delete from DbEventsDownSyncOperation")
    fun deleteAll()
}
