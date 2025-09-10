package com.simprints.infra.eventsync.event.commcare.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommCareSyncDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncedCase: SyncedCaseEntity)

    @Query("SELECT * FROM synced_commcare_cases WHERE caseId = :caseId")
    suspend fun getByCaseId(caseId: String): SyncedCaseEntity?

    @Query("SELECT * FROM synced_commcare_cases")
    suspend fun getAll(): List<SyncedCaseEntity>

    @Query("DELETE FROM synced_commcare_cases WHERE caseId = :caseId")
    suspend fun deleteByCaseId(caseId: String)

    @Query("DELETE FROM synced_commcare_cases")
    suspend fun clearAll()
}
