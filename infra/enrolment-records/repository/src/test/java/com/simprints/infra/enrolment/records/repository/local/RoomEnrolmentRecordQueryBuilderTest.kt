package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbSubject
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RoomEnrolmentRecordQueryBuilderTest {
    private lateinit var queryBuilder: RoomEnrolmentRecordQueryBuilder

    companion object {
        private const val SUBJECT_ID_1 = "subject_id_1"
        private const val SUBJECT_ID_2 = "subject_id_2"
        private const val SUBJECT_ID_3 = "subject_id_3"
        private const val AFTER_SUBJECT_ID = "after_subject_id"
        private const val PROJECT_ID = "project_id_x"
        private val ATTENDANT_ID = "attendant_id_y".asTokenizableEncrypted()
        private val MODULE_ID = "module_id_z".asTokenizableEncrypted()
        private const val FP_FORMAT = "FP_FORMAT_XYZ"
        private const val FACE_FORMAT = "FACE_FORMAT_ABC"
        private const val PAGE_SIZE = 10
        private const val LAST_SEEN_SUBJECT_ID = "last_seen_subj_001"
        private const val SUBJECT_TABLE_NAME = DbSubject.SUBJECT_TABLE_NAME
        private const val TEMPLATE_TABLE_NAME = DbBiometricTemplate.TEMPLATE_TABLE_NAME
        private const val SUBJECT_ID_COLUMN = DbSubject.SUBJECT_ID_COLUMN
        private const val PROJECT_ID_COLUMN = DbSubject.PROJECT_ID_COLUMN
        private const val ATTENDANT_ID_COLUMN = DbSubject.ATTENDANT_ID_COLUMN
        private const val MODULE_ID_COLUMN = DbSubject.MODULE_ID_COLUMN
        private const val FORMAT_COLUMN = DbBiometricTemplate.FORMAT_COLUMN
    }

    @Before
    fun setUp() {
        queryBuilder = RoomEnrolmentRecordQueryBuilder()
    }

    // region buildWhereClause Tests
    @Test
    fun `buildWhereClause with empty query returns empty clause and args`() {
        val query = SubjectQuery()
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEmpty()
        assertThat(args).isEmpty()
    }

    @Test
    fun `buildWhereClause with subjectId`() {
        val query = SubjectQuery(subjectId = SUBJECT_ID_1)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$SUBJECT_ID_COLUMN = ?")
        assertThat(args).containsExactly(SUBJECT_ID_1)
    }

    @Test
    fun `buildWhereClause with subjectIds not empty`() {
        val query = SubjectQuery(subjectIds = listOf(SUBJECT_ID_1, SUBJECT_ID_2))
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$SUBJECT_ID_COLUMN IN (?,?)")
        assertThat(args).containsExactly(SUBJECT_ID_1, SUBJECT_ID_2).inOrder()
    }

    @Test
    fun `buildWhereClause with subjectIds empty`() {
        val query = SubjectQuery(subjectIds = emptyList())
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEmpty()
        assertThat(args).isEmpty()
    }

    @Test
    fun `buildWhereClause with afterSubjectId`() {
        val query = SubjectQuery(afterSubjectId = AFTER_SUBJECT_ID)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$SUBJECT_ID_COLUMN > ?")
        assertThat(args).containsExactly(AFTER_SUBJECT_ID)
    }

    @Test
    fun `buildWhereClause with projectId`() {
        val query = SubjectQuery(projectId = PROJECT_ID)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$PROJECT_ID_COLUMN = ?")
        assertThat(args).containsExactly(PROJECT_ID)
    }

    @Test
    fun `buildWhereClause with attendantId`() {
        val query = SubjectQuery(attendantId = ATTENDANT_ID)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$ATTENDANT_ID_COLUMN = ?")
        assertThat(args).containsExactly(ATTENDANT_ID.value)
    }

    @Test
    fun `buildWhereClause with moduleId`() {
        val query = SubjectQuery(moduleId = MODULE_ID)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE S.$MODULE_ID_COLUMN = ?")
        assertThat(args).containsExactly(MODULE_ID.value)
    }

    @Test
    fun `buildWhereClause with faceSampleFormat`() {
        val query = SubjectQuery(faceSampleFormat = FACE_FORMAT)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE T.$FORMAT_COLUMN = ?")
        assertThat(args).containsExactly(FACE_FORMAT)
    }

    @Test
    fun `buildWhereClause with fingerprintSampleFormat`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT)
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo("WHERE T.$FORMAT_COLUMN = ?")
        assertThat(args).containsExactly(FP_FORMAT)
    }

    @Test
    fun `buildWhereClause with multiple subject fields`() {
        val query = SubjectQuery(
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
        )
        val (clause, args) = queryBuilder.buildWhereClause(query)
        assertThat(clause).isEqualTo(
            "WHERE S.$PROJECT_ID_COLUMN = ? AND S.$ATTENDANT_ID_COLUMN = ? AND S.$MODULE_ID_COLUMN = ?",
        )
        assertThat(args).containsExactly(PROJECT_ID, ATTENDANT_ID.value, MODULE_ID.value).inOrder()
    }

    @Test
    fun `buildWhereClause with all parameters`() {
        val query = SubjectQuery(
            subjectId = SUBJECT_ID_1,
            subjectIds = listOf(SUBJECT_ID_2, SUBJECT_ID_3),
            afterSubjectId = AFTER_SUBJECT_ID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            fingerprintSampleFormat = FP_FORMAT,
        )
        val (clause, args) = queryBuilder.buildWhereClause(query)
        val expectedClause =
            "WHERE S.$SUBJECT_ID_COLUMN = ? AND S.$SUBJECT_ID_COLUMN IN (?,?) AND S.$SUBJECT_ID_COLUMN > ? AND S.$PROJECT_ID_COLUMN = ? AND S.$ATTENDANT_ID_COLUMN = ? AND S.$MODULE_ID_COLUMN = ? AND T.$FORMAT_COLUMN = ?"
        assertThat(clause).isEqualTo(expectedClause)
        assertThat(args)
            .containsExactly(
                SUBJECT_ID_1,
                SUBJECT_ID_2,
                SUBJECT_ID_3,
                AFTER_SUBJECT_ID,
                PROJECT_ID,
                ATTENDANT_ID.value,
                MODULE_ID.value,
                FP_FORMAT,
            ).inOrder()
    }

    @Test
    fun `buildWhereClause with custom aliases`() {
        val query = SubjectQuery(subjectId = SUBJECT_ID_1, fingerprintSampleFormat = FP_FORMAT)
        val (clause, args) = queryBuilder.buildWhereClause(
            query,
            subjectAlias = "customS.",
            templateAlias = "customT.",
        )
        val expectedClause =
            "WHERE customS.$SUBJECT_ID_COLUMN = ? AND customT.$FORMAT_COLUMN = ?"
        assertThat(clause).isEqualTo(expectedClause)
        assertThat(args).containsExactly(SUBJECT_ID_1, FP_FORMAT).inOrder()
    }

    @Test
    fun `buildWhereClause throws error if both fingerprint and face format set`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT, faceSampleFormat = FACE_FORMAT)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildWhereClause(query)
        }
        assertThat(exception.message).isEqualTo("Cannot set both fingerprintSampleFormat and faceSampleFormat")
    }
    // endregion

    // region buildSubjectQuery Tests
    @Test
    fun `buildSubjectQuery with empty query`() {
        val query = SubjectQuery()
        val result = queryBuilder.buildSubjectQuery(query)
        val expectedSql =
            """
            SELECT * FROM DbSubject S
            
            
            """.trimIndent()
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(0)
    }

    @Test
    fun `buildSubjectQuery with projectId`() {
        val query = SubjectQuery(projectId = PROJECT_ID)
        val result = queryBuilder.buildSubjectQuery(query)
        val expectedSql =
            """
            SELECT * FROM DbSubject S
            WHERE S.projectId = ?
            
            """.trimIndent()
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildSubjectQuery with multiple subject fields`() {
        val query = SubjectQuery(projectId = PROJECT_ID, attendantId = ATTENDANT_ID)
        val result = queryBuilder.buildSubjectQuery(query)
        val expectedSql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            WHERE S.$PROJECT_ID_COLUMN = ? AND S.$ATTENDANT_ID_COLUMN = ?
            
            """.trimIndent()
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(2)
    }

    @Test
    fun `buildSubjectQuery with sort true`() {
        val query = SubjectQuery(sort = true)
        val result = queryBuilder.buildSubjectQuery(query)
        val expectedSql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            
            ORDER BY S.$SUBJECT_ID_COLUMN ASC
            """.trimIndent()
        // Note the space before ORDER due to buildWhereClause
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(0)
    }

    @Test
    fun `buildSubjectQuery with sort true and projectId`() {
        val query = SubjectQuery(projectId = PROJECT_ID, sort = true)
        val result = queryBuilder.buildSubjectQuery(query)
        val expectedSql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            WHERE S.$PROJECT_ID_COLUMN = ?
            ORDER BY S.$SUBJECT_ID_COLUMN ASC
            """.trimIndent()
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildSubjectQuery throws error if fingerprintSampleFormat is set`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildSubjectQuery(query)
        }
        assertThat(exception.message).isEqualTo("Cannot set format for subject query, use buildBiometricTemplatesQuery instead")
    }

    @Test
    fun `buildSubjectQuery throws error if faceSampleFormat is set`() {
        val query = SubjectQuery(faceSampleFormat = FACE_FORMAT)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildSubjectQuery(query)
        }
        assertThat(exception.message).isEqualTo("Cannot set format for subject query, use buildBiometricTemplatesQuery instead")
    }
    // endregion

    // region buildCountQuery Tests
    @Test
    fun `buildCountQuery with empty query no format`() {
        val query = SubjectQuery()
        val result = queryBuilder.buildCountQuery(query)
        assertThat(result.sql).isEqualTo("SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S ")
        assertThat(result.argCount).isEqualTo(0)
    }

    @Test
    fun `buildCountQuery with projectId no format`() {
        val query = SubjectQuery(projectId = PROJECT_ID)
        val result = queryBuilder.buildCountQuery(query)
        assertThat(
            result.sql,
        ).isEqualTo("SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S WHERE S.$PROJECT_ID_COLUMN = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery with fingerprintSampleFormat no other clauses`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT)
        val result = queryBuilder.buildCountQuery(query)
        assertThat(result.sql).isEqualTo(
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using($SUBJECT_ID_COLUMN) WHERE T.$FORMAT_COLUMN = ?",
        )
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery with faceSampleFormat and projectId`() {
        val query = SubjectQuery(faceSampleFormat = FACE_FORMAT, projectId = PROJECT_ID)
        val result = queryBuilder.buildCountQuery(query)
        val expectedSql = "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
            " using($SUBJECT_ID_COLUMN) WHERE S.$PROJECT_ID_COLUMN = ? AND T.$FORMAT_COLUMN = ?"
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(2)
    }

    @Test
    fun `buildCountQuery with fingerprintSampleFormat`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT)
        val result = queryBuilder.buildCountQuery(query)
        val expectedSql = "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
            " using($SUBJECT_ID_COLUMN) WHERE T.$FORMAT_COLUMN = ?"
        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery throws error if both fingerprint and face format set`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT, faceSampleFormat = FACE_FORMAT)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildCountQuery(query) // This will call buildWhereClause
        }
        assertThat(exception.message).isEqualTo("Cannot set both fingerprintSampleFormat and faceSampleFormat")
    }
    // endregion

    // region buildBiometricTemplatesQuery Tests
    @Test
    fun `buildBiometricTemplatesQuery with fingerprintSampleFormat, no lastSeenId`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT)
        val result = queryBuilder.buildBiometricTemplatesQuery(query, PAGE_SIZE, null)

        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.$SUBJECT_ID_COLUMN
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING($SUBJECT_ID_COLUMN)
                WHERE T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $PAGE_SIZE
            ) B USING($SUBJECT_ID_COLUMN) where A.$FORMAT_COLUMN ='$FP_FORMAT'
            """.trimIndent()

        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildBiometricTemplatesQuery with faceSampleFormat and lastSeenId`() {
        val query = SubjectQuery(faceSampleFormat = FACE_FORMAT)
        val result = queryBuilder.buildBiometricTemplatesQuery(query, PAGE_SIZE, LAST_SEEN_SUBJECT_ID)

        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.$SUBJECT_ID_COLUMN
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING($SUBJECT_ID_COLUMN)
                WHERE S.$SUBJECT_ID_COLUMN > ? AND T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $PAGE_SIZE
            ) B USING($SUBJECT_ID_COLUMN) where A.$FORMAT_COLUMN ='$FACE_FORMAT'
            """.trimIndent()

        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(2)
    }

    @Test
    fun `buildBiometricTemplatesQuery with fingerprintFormat, projectId, lastSeenId`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT, projectId = PROJECT_ID)
        val result = queryBuilder.buildBiometricTemplatesQuery(query, PAGE_SIZE, LAST_SEEN_SUBJECT_ID)

        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.$SUBJECT_ID_COLUMN
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING($SUBJECT_ID_COLUMN)
                WHERE S.$SUBJECT_ID_COLUMN > ? AND S.$PROJECT_ID_COLUMN = ? AND T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $PAGE_SIZE
            ) B USING($SUBJECT_ID_COLUMN) where A.$FORMAT_COLUMN ='$FP_FORMAT'
            """.trimIndent()

        assertThat(result.sql).isEqualTo(expectedSql)
        assertThat(result.argCount).isEqualTo(3)
    }

    @Test
    fun `buildBiometricTemplatesQuery throws error if no format is set`() {
        val query = SubjectQuery() // No format
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildBiometricTemplatesQuery(query, PAGE_SIZE)
        }
        assertThat(
            exception.message,
        ).isEqualTo("Must set format for biometric templates query, use buildSubjectQuery or buildCountQuery instead")
    }

    @Test
    fun `buildBiometricTemplatesQuery throws error if both fingerprint and face format set`() {
        val query = SubjectQuery(fingerprintSampleFormat = FP_FORMAT, faceSampleFormat = FACE_FORMAT)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            queryBuilder.buildBiometricTemplatesQuery(query, PAGE_SIZE)
        }
        assertThat(exception.message).isEqualTo("Cannot set both fingerprintSampleFormat and faceSampleFormat")
    }
    // endregion
}
