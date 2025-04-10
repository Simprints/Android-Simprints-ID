package com.simprints.infra.enrolment.records.room.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SupportSQLiteQuery
import com.simprints.infra.enrolment.records.room.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.room.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap
import com.simprints.infra.enrolment.records.room.store.models.FingerIdentifierAndTemplate
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: DbSubject)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFingerprintSamples(fingerprintSamples: List<DbFingerprintSample>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFaceSamples(faceSamples: List<DbFaceSample>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectTemplateMapping(mapping: DbSubjectTemplateFormatMap)

    @Query("DELETE FROM DbSubject WHERE subjectId = :subjectId")
    suspend fun deleteSubject(subjectId: String)

    @RawQuery
    suspend fun deleteSubjects(query: SupportSQLiteQuery): Int

    @Query("DELETE FROM DbFaceSample WHERE uuid = :uuid")
    suspend fun deleteFaceSample(uuid: String)

    @Query("DELETE FROM DbFingerprintSample WHERE uuid = :uuid")
    suspend fun deleteFingerprintSample(uuid: String)

    @Query("SELECT * FROM DbSubject WHERE subjectId = :subjectId")
    suspend fun getSubject(subjectId: String): SubjectBiometrics?

    @RawQuery
    suspend fun loadSubjects(query: SupportSQLiteQuery): List<SubjectBiometrics>

    @RawQuery
    suspend fun count(query: SupportSQLiteQuery): Int

    @Query(
        """
        SELECT fs.subjectId, fs.fingerIdentifier, fs.template
        FROM DbFingerprintSample fs
        JOIN (
            SELECT s.subjectId
            FROM DbSubject s
            INNER JOIN DbSubjectTemplateFormatMap m ON s.subjectId = m.subjectId
            WHERE m.format = :format
              AND (:projectId IS NULL OR s.projectId = :projectId)
              AND (:subjectId IS NULL OR s.subjectId = :subjectId)
              AND (:attendantId IS NULL OR s.attendantId = :attendantId)
              AND (:moduleId IS NULL OR s.moduleId = :moduleId)
            ORDER BY s.createdAt
            LIMIT :limit OFFSET :offset
        ) AS filterSubjects
        ON fs.subjectId = filterSubjects.subjectId
        GROUP BY fs.subjectId
        """,
    )
    suspend fun loadFingerprintSamples(
        format: String,
        projectId: String?,
        subjectId: String?,
        attendantId: String?,
        moduleId: String?,
        offset: Int,
        limit: Int,
    ): Map<
        @MapColumn(DbSubject.SUBJECT_ID_COLUMN) String,
        List<FingerIdentifierAndTemplate>,
    >

    @Query(
        """
        SELECT fs.subjectId, fs.template
        FROM DbFaceSample fs
        JOIN (
            SELECT s.subjectId
            FROM DbSubject s
            INNER JOIN DbSubjectTemplateFormatMap m ON s.subjectId = m.subjectId
            WHERE m.format = :format
              AND (:projectId IS NULL OR s.projectId = :projectId)
              AND (:subjectId IS NULL OR s.subjectId = :subjectId)
              AND (:attendantId IS NULL OR s.attendantId = :attendantId)
              AND (:moduleId IS NULL OR s.moduleId = :moduleId)
            ORDER BY s.createdAt
            LIMIT :limit OFFSET :offset
        ) AS filterSubjects
        ON fs.subjectId = filterSubjects.subjectId
        GROUP BY fs.subjectId
        """,
    )
    suspend fun loadFaceSamples(
        format: String,
        projectId: String?,
        subjectId: String?,
        attendantId: String?,
        moduleId: String?,
        offset: Int,
        limit: Int,
    ): Map<
        @MapColumn(DbSubject.SUBJECT_ID_COLUMN) String,
        List<@MapColumn("template") ByteArray>,
    >
}
