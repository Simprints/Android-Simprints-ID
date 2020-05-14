package com.simprints.id.data.db.subjects_sync.up.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubjectsUpSyncOperationLocalDataSource {

    @Query("select * from UpSyncStatus where id LIKE :key")
    suspend fun getUpSyncOperation(key: DbUpSyncOperationKey): DbSubjectsUpSyncOperation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceUpSyncOperation(subjectsUpSyncStatus: DbSubjectsUpSyncOperation)

    @Query("delete from UpSyncStatus")
    suspend fun deleteAll()
}
