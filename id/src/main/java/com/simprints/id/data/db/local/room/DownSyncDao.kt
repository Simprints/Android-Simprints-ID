package com.simprints.id.data.db.local.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

@Dao
interface DownSyncDao {

    @Query("select * from DownSyncStatus")
    fun getDownSyncStatusLiveData(): LiveData<List<DownSyncStatus>>

    @Query("select * from DownSyncStatus")
    fun getDownSyncStatus(): List<DownSyncStatus>

    @Query("select * from DownSyncStatus where id = :downSyncStatusId")
    fun getDownSyncStatusForId(downSyncStatusId: String): DownSyncStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceDownSyncStatus(downSyncStatus: DownSyncStatus)

    @Transaction
    fun insertOrReplaceDownSyncStatuses(downSyncStatus: List<DownSyncStatus>) {
        downSyncStatus.forEach { insertOrReplaceDownSyncStatus(it) }
    }

    @Query("update DownSyncStatus set totalToDownload = :totalToDownload where id = :downSyncStatusId")
    fun updatePeopleToDownSync(downSyncStatusId: String, totalToDownload: Int)

    @Query("update DownSyncStatus set lastSyncTime = :lastSyncTime where id = :downSyncStatusId")
    fun updateLastSyncTime(downSyncStatusId: String, lastSyncTime: Long)

    @Query("update DownSyncStatus set lastPatientId = :lastPatientId where id = :downSyncStatusId")
    fun updateLastPatientId(downSyncStatusId: String, lastPatientId: String)

    @Query("update DownSyncStatus set lastPatientUpdatedAt = :lastPatientUpdatedAt where id = :downSyncStatusId")
    fun updateLastPatientUpdatedAt(downSyncStatusId: String, lastPatientUpdatedAt: Long)

    @Query("delete from DownSyncStatus  where id = :downSyncStatusId")
    fun deleteDownSyncStatus(downSyncStatusId: String)

    @Query("delete from DownSyncStatus")
    fun deleteAll()
}
fun DownSyncDao.getStatusId(projectId: String, userId: String?, moduleId: String?) = "${projectId}_${userId ?: ""}_${moduleId ?: ""}"
fun DownSyncDao.getStatusId(subSyncScope: SubSyncScope) = getStatusId(subSyncScope.projectId, subSyncScope.userId, subSyncScope.moduleId)
