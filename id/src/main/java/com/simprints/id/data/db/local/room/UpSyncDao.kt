package com.simprints.id.data.db.local.room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.id.data.db.local.room.UpSyncStatus.Companion.UP_SYNC_STATUS_CONST_ID

/**
 * Aashay - UpSyncDao: Dao to make queries for UpSyncDao
 */
@Dao
interface UpSyncDao {

    @Query("select * from UpSyncStatus where upSyncStatusId = '$UP_SYNC_STATUS_CONST_ID'")
    fun getUpSyncStatus(): LiveData<UpSyncStatus?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLastUpSyncTime(upSyncStatus: UpSyncStatus)
}
