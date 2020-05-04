package com.simprints.id.data.db.subjects_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubjectsDownSyncOperationLocalDataSource {

    @Query("select * from DbSubjectsDownSyncOperation where id=:key")
    suspend fun getDownSyncOperation(key: DbPeopleDownSyncOperationKey): List<DbSubjectsDownSyncOperation>

    @Query("select * from DbSubjectsDownSyncOperation")
    suspend fun getDownSyncOperationsAll(): List<DbSubjectsDownSyncOperation>

    @Query("select * from DbSubjectsDownSyncOperation")
    fun getDownSyncOperation(): List<DbSubjectsDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDownSyncOperation(dbSubjectsDownSyncOperation: DbSubjectsDownSyncOperation)

    @Query("delete from DbSubjectsDownSyncOperation")
    fun deleteAll()
}
