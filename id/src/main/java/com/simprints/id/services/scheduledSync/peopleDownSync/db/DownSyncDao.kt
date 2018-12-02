package com.simprints.id.services.scheduledSync.peopleDownSync.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownSyncDao {

    @Query("select * from DownSyncStatus")
    fun getDownSyncStatus(): LiveData<List<DownSyncStatus>>

    @Query("select * from DownSyncStatus where id = :downSyncStatusId")
    fun getDownSyncStatusForId(downSyncStatusId: String): DownSyncStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDownSyncStatus(downSyncStatus: DownSyncStatus)

    @Query("update DownSyncStatus set totalToDownload = :totalToDownload where id = :downSyncStatusId")
    fun updatePeopleToDownSync(downSyncStatusId: String, totalToDownload: Int)

    @Query("update DownSyncStatus set lastSyncTime = :lastSyncTime where id = :downSyncStatusId")
    fun updateLastSyncTime(downSyncStatusId: String, lastSyncTime: Long)

    @Query("update DownSyncStatus set lastPatientId = :lastPatientId where id = :downSyncStatusId")
    fun updateLastPatientId(downSyncStatusId: String, lastPatientId: String)

    @Query("update DownSyncStatus set lastPatientUpdatedAt = :lastPatientUpdatedAt where id = :downSyncStatusId")
    fun updateLastPatientUpdatedAt(downSyncStatusId: String, lastPatientUpdatedAt: Long)

    // STOPSHIP
    @Query("delete from DownSyncStatus")
    fun lolDelete()
}
fun DownSyncDao.getStatusId(projectId: String, userId: String?, moduleId: String?) = "${projectId}_${userId ?: ""}_${moduleId ?: ""}"
