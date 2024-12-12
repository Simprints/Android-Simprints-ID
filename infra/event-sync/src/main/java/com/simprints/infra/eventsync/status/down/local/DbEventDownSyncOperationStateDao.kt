package com.simprints.infra.eventsync.status.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface DbEventDownSyncOperationStateDao {
    @Query("select * from DbEventsDownSyncOperation")
    suspend fun load(): List<DbEventsDownSyncOperationState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dbEventsDownSyncOperationState: DbEventsDownSyncOperationState)

    @Query("delete from DbEventsDownSyncOperation where id = :id")
    suspend fun delete(id: String)

    @Query("delete from DbEventsDownSyncOperation")
    suspend fun deleteAll()
}
