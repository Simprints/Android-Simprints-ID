package com.simprints.id.services.scheduledSync.peopleDownSync.newplan.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Aashay - DownSyncDao: Dao to make queries for DownSyncStatus
 */

@Dao
interface DownSyncDao {

    @Query("select * from DownSyncStatus")
    fun getDownSyncStatus(): LiveData<List<DownSyncStatus>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDownSyncStatus(downSyncStatus: DownSyncStatus)

    @Query("update DownSyncStatus set totalToDownload = :totalToDownload where id = :downSyncStatusId")
    fun updatePeopleToDownSync(downSyncStatusId: String, totalToDownload: Int)

    @Query("update DownSyncStatus set lastSyncTime = :lastSyncTime where id = :downSyncStatusId")
    fun updateLastSyncTime(downSyncStatusId: String, lastSyncTime: Long)

    @Query("update DownSyncStatus set lastPatientId = :lastPatientId where id = :downSyncStatusId")
    fun updateLastPatientId(downSyncStatusId: String, lastPatientId: String)

    @Query("update DownSyncStatus set lastPatientUpdatedAt = :lastPatientUpdatedAt where id = :downSyncStatusId")
    fun updatelastPatientUpdatedAt(downSyncStatusId: String, lastPatientUpdatedAt: Long)
}
