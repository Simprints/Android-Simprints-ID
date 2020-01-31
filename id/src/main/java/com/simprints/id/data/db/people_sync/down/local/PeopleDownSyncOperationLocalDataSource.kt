package com.simprints.id.data.db.people_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PeopleDownSyncOperationLocalDataSource {

    @Query("select * from DbPeopleDownSyncOperation where id=:key")
    suspend fun getDownSyncOperation(key: DbPeopleDownSyncOperationKey): List<DbPeopleDownSyncOperation>

    @Query("select * from DbPeopleDownSyncOperation")
    suspend fun getDownSyncOperationsAll(): List<DbPeopleDownSyncOperation>

    @Query("select * from DbPeopleDownSyncOperation")
    fun getDownSyncOperation(): List<DbPeopleDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDownSyncOperation(dbPeopleDownSyncOperation: DbPeopleDownSyncOperation)

    @Query("delete from DbPeopleDownSyncOperation")
    fun deleteAll()
}
