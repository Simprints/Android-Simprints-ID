package com.simprints.id.data.db.people_sync.up.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PeopleUpSyncOperationLocalDataSource {

    @Query("select * from UpSyncStatus where id LIKE :key")
    suspend fun getUpSyncOperation(key: DbUpSyncOperationKey): DbUpSyncOperation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceUpSyncOperation(upSyncStatus: DbUpSyncOperation)

    @Query("delete from UpSyncStatus")
    suspend fun deleteAll()
}
