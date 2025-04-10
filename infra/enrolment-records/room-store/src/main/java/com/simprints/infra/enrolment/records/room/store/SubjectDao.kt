package com.simprints.infra.enrolment.records.room.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: DbSubject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiometricSamples(samples: List<DbBiometricTemplate>)

    @Query("DELETE FROM DbSubject WHERE subjectId = :subjectId")
    suspend fun deleteSubject(subjectId: String)

    @Query("DELETE FROM DbBiometricTemplate WHERE uuid = :uuid")
    suspend fun deleteBiometricSample(uuid: String)

    @RawQuery
    suspend fun deleteSubjects(query: SupportSQLiteQuery): Int

    @Query("SELECT * FROM DbSubject WHERE subjectId = :subjectId")
    suspend fun getSubject(subjectId: String): SubjectBiometrics?

    @RawQuery
    suspend fun loadSubjects(query: SupportSQLiteQuery): List<SubjectBiometrics>

    @RawQuery
    suspend fun countSubjects(query: SupportSQLiteQuery): Int

    @RawQuery
    suspend fun loadSamples(query: SupportSQLiteQuery): Map<@MapColumn(DbSubject.SUBJECT_ID_COLUMN) String, List<DbBiometricTemplate>>
}
