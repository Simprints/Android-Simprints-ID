package com.simprints.id.data.db.people_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubjectsDownSyncOperationLocalDataSource {

    @Query("select * from DbPeopleDownSyncOperation where id=:key")
    suspend fun getDownSyncOperation(key: DbPeopleDownSyncOperationKey): List<DbSubjectsDownSyncOperation>

    @Query("select * from DbPeopleDownSyncOperation")
    suspend fun getDownSyncOperationsAll(): List<DbSubjectsDownSyncOperation>

    @Query("select * from DbPeopleDownSyncOperation")
    fun getDownSyncOperation(): List<DbSubjectsDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDownSyncOperation(dbSubjectsDownSyncOperation: DbSubjectsDownSyncOperation)

    @Query("delete from DbPeopleDownSyncOperation")
    fun deleteAll()
}
