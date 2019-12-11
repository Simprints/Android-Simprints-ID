package com.simprints.id.data.db.syncstatus.downsyncinfo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.syncscope.local.DbDownSyncOperation

@Dao
interface DownSyncOperationDao {

    @Query("select * from DbDownSyncOperation")
    fun getDownSyncOperation(): LiveData<List<DbDownSyncOperation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDownSyncStatus(dbDownSyncOperation: DbDownSyncOperation)

    @Query("delete from DbDownSyncOperation")
    fun deleteAll()
}
