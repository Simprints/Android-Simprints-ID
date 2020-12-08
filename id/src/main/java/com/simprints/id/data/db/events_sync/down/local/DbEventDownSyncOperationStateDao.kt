package com.simprints.id.data.db.events_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DbEventDownSyncOperationStateDao {

    @Query("select * from DbEventsDownSyncOperation")
    suspend fun load(): List<DbEventsDownSyncOperationState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEventsDownSyncOperationState: DbEventsDownSyncOperationState)

    @Query("delete from DbEventsDownSyncOperation")
    fun deleteAll()
}
