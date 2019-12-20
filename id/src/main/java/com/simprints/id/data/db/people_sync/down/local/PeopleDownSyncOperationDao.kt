package com.simprints.id.data.db.people_sync.down.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PeopleDownSyncDao {

    @Query("select * from DbDownSyncOperation where id=:key")
    suspend fun getDownSyncOperation(key: DbDownSyncOperationKey): List<DbDownSyncOperation>

    @Query("select * from DbDownSyncOperation")
    suspend fun getDownSyncOperationAll(): List<DbDownSyncOperation>

    @Query("select * from DbDownSyncOperation")
    fun getDownSyncOperation(): List<DbDownSyncOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplaceDownSyncOperation(dbDownSyncOperation: DbDownSyncOperation)

    @Query("delete from DbDownSyncOperation")
    fun deleteAll()
}
