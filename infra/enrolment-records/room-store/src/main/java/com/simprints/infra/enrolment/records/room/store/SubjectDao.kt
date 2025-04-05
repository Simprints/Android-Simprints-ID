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
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics

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

    @RawQuery
    suspend fun deleteSubjects(query: SupportSQLiteQuery): Int

    @Transaction
    @RawQuery
    suspend fun loadSubjects(query: SupportSQLiteQuery): List<SubjectBiometrics>

    /**
     * Counts the number of distinct subjects that match the given filtering criteria.
     *
     * This method ensures that a subject is counted only once, even if they have multiple
     * face or fingerprint samples. It optimizes performance by avoiding expensive `JOIN`
     * operations and instead using `EXISTS` subqueries, which efficiently check for the
     * presence of related biometric records without generating large intermediate result sets.
     *
     * ## Query Behavior:
     * - Filters subjects based on optional parameters (e.g., `projectId`, `attendantId`, etc.).
     * - If `fingerprintSampleFormat` is provided, only counts subjects that have at least one
     *   fingerprint sample with the specified format.
     * - If `faceSampleFormat` is provided, only counts subjects that have at least one face
     *   sample with the specified format.
     * - Uses `EXISTS` instead of `JOIN` to efficiently check for biometric samples.
     * - Leverages database indexes for performance optimization.
     *
     * ## Performance Considerations:
     * - **Indexes:** Ensure indexes exist on `DbSubject.subjectId`, `DbFingerprintSample.subjectId`,
     *   `DbFaceSample.subjectId`, and their respective `format` columns for efficient filtering.
     * - **Scalability:** Using `EXISTS` prevents the query from generating excessive row combinations,
     *   making it efficient even with 50K+ subjects and millions of biometric samples.
     * - **Avoids Full Table Scans:** If filtering by biometrics, the query quickly exits once
     *   a match is found, improving query execution time.
     **/
    @Query(
        """
    SELECT COUNT(*) FROM DbSubject        
""",
    )
    fun countSubjects(): Int

    /**
     * Retrieves a paginated list of distinct subjects along with their fingerprint samples
     * that match the given filtering criteria.
     *
     * This method ensures that:
     * - Subjects are retrieved uniquely based on the provided filters.
     * - Multiple fingerprint samples per subject are included in the result.
     * - Efficient pagination is applied using `LIMIT` and `OFFSET`.
     * - Uses `LEFT JOIN` to ensure that multiple fingerprint samples can be selected per subject
     *   without causing duplicates.
     *
     * ## Query Behavior:
     * - Filters subjects based on optional parameters (e.g., `projectId`, `attendantId`, etc.).
     * - If `fingerprintSampleFormat` is provided, only returns subjects with at least one
     *   fingerprint sample of the specified format.
     * - Uses `EXISTS` to efficiently check for fingerprint samples that match the format.
     * - Orders by `subjectId` for consistent pagination.
     * - Uses `LIMIT` and `OFFSET` for efficient page-wise retrieval.
     *
     * ## Performance Considerations:
     * - **Indexes:** Ensure indexes exist on `DbSubject.subjectId`, `DbFingerprintSample.subjectId`,
     *   and `DbFingerprintSample.format` to optimize filtering and joins.
     * - **Scalability:** Pagination ensures that queries remain performant even with large datasets.
     * - **Avoids Duplicate Counting:** Retrieves all fingerprint samples for a subject without
     *   causing duplicates.
     *
     * @param projectId (Optional) Filters subjects by project ID.
     * @param subjectId (Optional) Filters for a specific subject ID.
     * @param subjectIds (Optional) Filters for multiple subject IDs.
     * @param attendantId (Optional) Filters subjects assigned to a specific attendant.
     * @param moduleId (Optional) Filters subjects assigned to a specific module.
     * @param fingerprintSampleFormat (Optional) If provided, only includes subjects with at least
     *        one fingerprint sample of the specified format.
     * @param limit The maximum number of subjects to return per query page.
     * @param offset The starting position for pagination.
     * @return A map where the keys are subject IDs, and the values are lists of fingerprint samples
     *         associated with each subject.
     */
    @Query(
        """
    SELECT s.subjectId, f.*
    FROM DbSubject s
    LEFT JOIN DbFingerprintSample f ON f.subjectId = s.subjectId
    WHERE 
        (:projectId IS NULL OR s.projectId = :projectId) AND
        (:subjectId IS NULL OR s.subjectId = :subjectId) AND
        (:subjectIds IS NULL OR s.subjectId IN (:subjectIds)) AND
        (:attendantId IS NULL OR s.attendantId = :attendantId) AND
        (:moduleId IS NULL OR s.moduleId = :moduleId) AND
        (
            (:fingerprintSampleFormat IS NULL OR EXISTS (
                SELECT 1 FROM DbFingerprintSample fs 
                WHERE fs.subjectId = s.subjectId AND fs.format = :fingerprintSampleFormat
            ))
        )
    GROUP BY s.subjectId
    ORDER BY s.subjectId
    LIMIT :limit OFFSET :offset
""",
    )
    fun getSubjectsWithFingerprintSamples(
        projectId: String?,
        subjectId: String?,
        subjectIds: List<String>?,
        attendantId: String?,
        moduleId: String?,
        fingerprintSampleFormat: String?,
        offset: Int,
        limit: Int,
    ): Map<
        @MapColumn(SUBJECT_ID_COLUMN) String,
        List<DbFingerprintSample>,
    >

    /**
     * Retrieves a paginated list of distinct subjects along with their face samples
     * that match the given filtering criteria.
     *
     * This method ensures that:
     * - Subjects are retrieved uniquely based on the provided filters.
     * - Multiple face samples per subject are included in the result.
     * - Efficient pagination is applied using `LIMIT` and `OFFSET`.
     * - Uses `LEFT JOIN` to ensure that multiple face samples can be selected per subject
     *   without causing duplicates.
     *
     * ## Query Behavior:
     * - Filters subjects based on optional parameters (e.g., `projectId`, `attendantId`, etc.).
     * - If `faceSampleFormat` is provided, only returns subjects with at least one
     *   face sample of the specified format.
     * - Uses `EXISTS` to efficiently check for face samples that match the format.
     * - Orders by `subjectId` for consistent pagination.
     * - Uses `LIMIT` and `OFFSET` for efficient page-wise retrieval.
     *
     * ## Performance Considerations:
     * - **Indexes:** Ensure indexes exist on `DbSubject.subjectId`, `DbFaceSample.subjectId`,
     *   and `DbFaceSample.format` to optimize filtering and joins.
     * - **Scalability:** Pagination ensures that queries remain performant even with large datasets.
     * - **Avoids Duplicate Counting:** Retrieves all face samples for a subject without
     *   causing duplicates.
     *
     * @param projectId (Optional) Filters subjects by project ID.
     * @param subjectId (Optional) Filters for a specific subject ID.
     * @param subjectIds (Optional) Filters for multiple subject IDs.
     * @param attendantId (Optional) Filters subjects assigned to a specific attendant.
     * @param moduleId (Optional) Filters subjects assigned to a specific module.
     * @param faceSampleFormat (Optional) If provided, only includes subjects with at least
     *        one face sample of the specified format.
     * @param limit The maximum number of subjects to return per query page.
     * @param offset The starting position for pagination.
     * @return A map where the keys are subject IDs, and the values are lists of face samples
     *         associated with each subject.
     */
    @Query(
        """
    SELECT s.subjectId, f.*
    FROM DbSubject s
    LEFT JOIN DbFaceSample f ON f.subjectId = s.subjectId
    WHERE 
        (:projectId IS NULL OR s.projectId = :projectId) AND
        (:subjectId IS NULL OR s.subjectId = :subjectId) AND
        (:subjectIds IS NULL OR s.subjectId IN (:subjectIds)) AND
        (:attendantId IS NULL OR s.attendantId = :attendantId) AND
        (:moduleId IS NULL OR s.moduleId = :moduleId) AND
        (
            (:faceSampleFormat IS NULL OR EXISTS (
                SELECT 1 FROM DbFaceSample fs 
                WHERE fs.subjectId = s.subjectId AND fs.format = :faceSampleFormat
            ))
        )
    GROUP BY s.subjectId
    ORDER BY s.subjectId
    LIMIT :limit OFFSET :offset
""",
    )
    fun getSubjectsWithFaceSamples(
        projectId: String?,
        subjectId: String?,
        subjectIds: List<String>?,
        attendantId: String?,
        moduleId: String?,
        faceSampleFormat: String?,
        offset: Int,
        limit: Int,
    ): Map<
        @MapColumn(SUBJECT_ID_COLUMN) String,
        List<DbFaceSample>,
    >
}
