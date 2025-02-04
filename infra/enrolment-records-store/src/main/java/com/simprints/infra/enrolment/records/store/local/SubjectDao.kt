package com.simprints.infra.enrolment.records.store.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.simprints.infra.enrolment.records.store.local.models.DbFaceSample
import com.simprints.infra.enrolment.records.store.local.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.store.local.models.DbSubject
import com.simprints.infra.enrolment.records.store.local.models.Subject
import java.util.UUID

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: DbSubject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFingerprintSample(fingerprintSamples: List<DbFingerprintSample>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFaceSample(faceSamples: List<DbFaceSample>)

    @Transaction
    @Query("SELECT * FROM subjects WHERE subjectId = :id")
    suspend fun getSubjectWithSamples(id: UUID): Subject?

    @Delete
    suspend fun deleteSubject(subject: DbSubject)

    @Transaction
    @RawQuery
    suspend fun loadSubjects(query: SupportSQLiteQuery): List<Subject>

    @Transaction
    @RawQuery
    suspend fun count(query: SupportSQLiteQuery): Int
//
//    @Transaction
//    @RawQuery
//    suspend fun loadFingerprintIdentities(query: SupportSQLiteQuery): List<FingerprintIdentity>
//
//    @Transaction
//    @RawQuery
//    suspend fun loadFaceIdentities(query: SupportSQLiteQuery): List<FaceIdentity>
}
