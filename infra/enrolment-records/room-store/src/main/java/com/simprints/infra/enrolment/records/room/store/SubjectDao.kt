package com.simprints.infra.enrolment.records.room.store

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.simprints.infra.enrolment.records.room.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.room.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectWithSamples

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: DbSubject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFingerprintSamples(fingerprintSamples: List<DbFingerprintSample>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceSamples(faceSamples: List<DbFaceSample>)

    @Query("DELETE FROM DBSUBJECT WHERE subjectId = :subjectId")
    suspend fun deleteSubject(subjectId: String)

    @Transaction
    @RawQuery
    suspend fun loadSubjects(query: SupportSQLiteQuery): List<DbSubjectWithSamples>

    @Transaction
    @RawQuery
    suspend fun count(query: SupportSQLiteQuery): Int

    @Transaction
    @RawQuery
    fun getFaceSamples(query: SupportSQLiteQuery): PagingSource<Int, DbFaceSample>

    @Transaction
    @RawQuery
    fun getFingerprintSamples(query: SupportSQLiteQuery): PagingSource<Int, DbFingerprintSample>
}
