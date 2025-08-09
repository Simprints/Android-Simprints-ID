package com.simprints.infra.enrolment.records.room.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simprints.infra.enrolment.records.room.store.models.DbCommCareCase

@Dao
interface CommCareCaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCase(case: DbCommCareCase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCases(cases: List<DbCommCareCase>)

    @Query("SELECT * FROM DbCommCareCase WHERE caseId = :caseId")
    suspend fun getCase(caseId: String): DbCommCareCase?

    @Query("SELECT * FROM DbCommCareCase WHERE subjectId = :subjectId")
    suspend fun getCaseBySubjectId(subjectId: String): DbCommCareCase?

    @Query("SELECT * FROM DbCommCareCase")
    suspend fun getAllCases(): List<DbCommCareCase>

    @Query("DELETE FROM DbCommCareCase WHERE caseId = :caseId")
    suspend fun deleteCase(caseId: String)

    @Query("DELETE FROM DbCommCareCase WHERE subjectId = :subjectId")
    suspend fun deleteCaseBySubjectId(subjectId: String)

    @Query("DELETE FROM DbCommCareCase")
    suspend fun deleteAllCases()
}