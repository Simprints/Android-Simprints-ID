package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteProgram
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential.Companion.EXTERNAL_CREDENTIAL_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential.Companion.EXTERNAL_CREDENTIAL_VALUE_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_TABLE_NAME
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RoomEnrolmentRecordQueryBuilderTest {
    private lateinit var queryBuilder: RoomEnrolmentRecordQueryBuilder

    @Before
    fun setUp() {
        queryBuilder = RoomEnrolmentRecordQueryBuilder()
    }

    @Test
    fun `buildSubjectQuery with empty query returns select all`() {
        val subjectQuery = SubjectQuery()
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql)
        assertThat(resultQuery.argCount).isEqualTo(0)
    }

    @Test
    fun `buildSubjectQuery with subjectId`() {
        val subjectId = "test-subject-id"
        val subjectQuery = SubjectQuery(subjectId = subjectId)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$SUBJECT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(subjectId))
    }

    @Test
    fun `buildSubjectQuery with subjectIds`() {
        val subjectIds = listOf("id1", "id2", "id3")
        val subjectQuery = SubjectQuery(subjectIds = subjectIds)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$SUBJECT_ID_COLUMN IN (?,?,?)"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(subjectIds.toTypedArray())
    }

    @Test
    fun `buildSubjectQuery with afterSubjectId`() {
        val afterSubjectId = "last-subject-id"
        val subjectQuery = SubjectQuery(afterSubjectId = afterSubjectId)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$SUBJECT_ID_COLUMN > ?"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(afterSubjectId))
    }

    @Test
    fun `buildSubjectQuery with projectId`() {
        val projectId = "test-project-id"
        val subjectQuery = SubjectQuery(projectId = projectId)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$PROJECT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId))
    }

    @Test
    fun `buildSubjectQuery with attendantId`() {
        val attendantId = "test-attendant-id".asTokenizableEncrypted()
        val subjectQuery = SubjectQuery(attendantId = attendantId)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$ATTENDANT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(attendantId.value))
    }

    @Test
    fun `buildSubjectQuery with moduleId`() {
        val moduleId = "test-module-id".asTokenizableEncrypted()
        val subjectQuery = SubjectQuery(moduleId = moduleId)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$MODULE_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(moduleId.value))
    }

    @Test
    fun `buildSubjectQuery with sort true`() {
        val subjectQuery = SubjectQuery(sort = true)
        val expectedSql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            
            
            ORDER BY S.$SUBJECT_ID_COLUMN ASC
            """.trimIndent()

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)
        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(resultQuery.argCount).isEqualTo(0)
    }

    @Test
    fun `buildSubjectQuery with multiple parameters and sort`() {
        val projectId = "proj1"
        val attendantId = "att1".asTokenizableEncrypted()
        val subjectQuery = SubjectQuery(projectId = projectId, attendantId = attendantId, sort = true)
        val expectedSql = "SELECT * FROM $SUBJECT_TABLE_NAME S\n\n" +
            "WHERE S.$PROJECT_ID_COLUMN = ? AND S.$ATTENDANT_ID_COLUMN = ?\n" +
            "ORDER BY S.$SUBJECT_ID_COLUMN ASC"

        val resultQuery = queryBuilder.buildSubjectQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId, attendantId.value))
    }

    @Test
    fun `buildSubjectQuery throws error if fingerprintSampleFormat is set`() {
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = "ISO_19794_2_2005")
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildSubjectQuery(subjectQuery)
        }
        assertThat(exception.message).isEqualTo("Cannot set format for subject query, use buildBiometricTemplatesQuery instead")
    }

    @Test
    fun `buildSubjectQuery throws error if faceSampleFormat is set`() {
        val subjectQuery = SubjectQuery(faceSampleFormat = "RAW")
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildSubjectQuery(subjectQuery)
        }
        assertThat(exception.message).isEqualTo("Cannot set format for subject query, use buildBiometricTemplatesQuery instead")
    }

    @Test
    fun `buildCountQuery with empty query`() {
        val subjectQuery = SubjectQuery()
        val expectedSql = "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(resultQuery.argCount).isEqualTo(0)
    }

    @Test
    fun `buildCountQuery with subjectId`() {
        val subjectId = "s1"
        val subjectQuery = SubjectQuery(subjectId = subjectId)
        val expectedSql =
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S WHERE S.$SUBJECT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(subjectId))
    }

    @Test
    fun `buildCountQuery with projectId`() {
        val projectId = "p1"
        val subjectQuery = SubjectQuery(projectId = projectId)
        val expectedSql =
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S WHERE S.$PROJECT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId))
    }

    @Test
    fun `buildCountQuery with fingerprintSampleFormat`() {
        val format = "ISO_FP"
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = format)
        val expectedSql =
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using(subjectId) WHERE T.$FORMAT_COLUMN = ?"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(format))
    }

    @Test
    fun `buildCountQuery with faceSampleFormat`() {
        val format = "RAW_FACE"
        val subjectQuery = SubjectQuery(faceSampleFormat = format)
        val expectedSql =
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using(subjectId) WHERE T.$FORMAT_COLUMN = ?"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(format))
    }

    @Test
    fun `buildCountQuery with projectId and fingerprintSampleFormat`() {
        val projectId = "p1"
        val format = "ISO_FP"
        val subjectQuery = SubjectQuery(projectId = projectId, fingerprintSampleFormat = format)
        val expectedSql =
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using(subjectId) WHERE S.$PROJECT_ID_COLUMN = ? AND T.$FORMAT_COLUMN = ?"

        val resultQuery = queryBuilder.buildCountQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId, format))
    }

    @Test
    fun `buildCountQuery throws if both fingerprint and face formats set`() {
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = "FP_FORMAT", faceSampleFormat = "FACE_FORMAT")
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildCountQuery(subjectQuery)
        }
        assertThat(exception.message).isEqualTo("Cannot set both fingerprintSampleFormat and faceSampleFormat")
    }

    @Test
    fun `buildBiometricTemplatesQuery throws error if format is not set`() {
        val subjectQuery = SubjectQuery()
        val pageSize = 10
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildBiometricTemplatesQuery(subjectQuery, pageSize)
        }
        assertThat(
            exception.message,
        ).isEqualTo("Must set format for biometric templates query, use buildSubjectQuery or buildCountQuery instead")
    }

    @Test
    fun `buildBiometricTemplatesQuery with fingerprintSampleFormat`() {
        val format = "ISO_FP_TEMPLATE"
        val pageSize = 10
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = format)
        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.subjectId
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING(subjectId)
                WHERE T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $pageSize
            ) B USING(subjectId) where A.format ='$format'
            """.trimIndent()

        val resultQuery = queryBuilder.buildBiometricTemplatesQuery(subjectQuery, pageSize)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(format))
    }

    @Test
    fun `buildBiometricTemplatesQuery with faceSampleFormat and projectId`() {
        val format = "RAW_FACE_TEMPLATE"
        val projectId = "projX"
        val pageSize = 5
        val subjectQuery = SubjectQuery(faceSampleFormat = format, projectId = projectId)
        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.subjectId
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING(subjectId)
                WHERE S.$PROJECT_ID_COLUMN = ? AND T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $pageSize
            ) B USING(subjectId) where A.format ='$format'
            """.trimIndent()

        val resultQuery = queryBuilder.buildBiometricTemplatesQuery(subjectQuery, pageSize)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId, format))
    }

    @Test
    fun `buildBiometricTemplatesQuery uses sort true internally`() {
        val format = "ANY_FORMAT"
        val pageSize = 15
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = format, sort = false)
        val expectedSql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.subjectId
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING(subjectId)
                WHERE T.$FORMAT_COLUMN = ?
                ORDER BY S.$SUBJECT_ID_COLUMN ASC
                LIMIT $pageSize
            ) B USING(subjectId) where A.format ='$format'
            """.trimIndent()

        val resultQuery = queryBuilder.buildBiometricTemplatesQuery(subjectQuery, pageSize)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(format))
    }

    @Test
    fun `buildBiometricTemplatesQuery throws if both fingerprint and face formats set`() {
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = "FP_FORMAT", faceSampleFormat = "FACE_FORMAT")
        val pageSize = 10
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildBiometricTemplatesQuery(subjectQuery, pageSize)
        }
        assertThat(exception.message).isEqualTo("Cannot set both fingerprintSampleFormat and faceSampleFormat")
    }

    @Test
    fun `buildDeleteQuery throws error if fingerprintSampleFormat is set`() {
        val subjectQuery = SubjectQuery(fingerprintSampleFormat = "ISO_19794_2_2005")
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildDeleteQuery(subjectQuery)
        }
        assertThat(exception.message).isEqualTo("faceSampleFormat and fingerprintSampleFormat are not supported for deletion")
    }

    @Test
    fun `buildDeleteQuery throws error if faceSampleFormat is set`() {
        val subjectQuery = SubjectQuery(faceSampleFormat = "RAW")
        val exception = assertThrows<IllegalArgumentException> {
            queryBuilder.buildDeleteQuery(subjectQuery)
        }
        assertThat(exception.message).isEqualTo("faceSampleFormat and fingerprintSampleFormat are not supported for deletion")
    }

    @Test
    fun `buildDeleteQuery with empty query`() {
        val subjectQuery = SubjectQuery()
        val expectedSql = "DELETE FROM DbSubject"

        val resultQuery = queryBuilder.buildDeleteQuery(subjectQuery)

        assertThat(resultQuery.sql.trim()).isEqualTo(expectedSql.trim())
        assertThat(resultQuery.argCount).isEqualTo(0)
    }

    @Test
    fun `buildDeleteQuery with subjectId`() {
        val subjectId = "id-to-delete"
        val subjectQuery = SubjectQuery(subjectId = subjectId)
        val expectedSql = "DELETE FROM DbSubject WHERE $SUBJECT_ID_COLUMN = ?"

        val resultQuery = queryBuilder.buildDeleteQuery(subjectQuery)

        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(subjectId))
    }

    @Test
    fun `buildDeleteQuery with projectId and moduleId`() {
        val projectId = "proj-del"
        val moduleId = "mod-del".asTokenizableEncrypted()
        val subjectQuery = SubjectQuery(projectId = projectId, moduleId = moduleId)
        val expectedSql = "DELETE FROM DbSubject WHERE $PROJECT_ID_COLUMN = ? AND $MODULE_ID_COLUMN = ?"
        val resultQuery = queryBuilder.buildDeleteQuery(subjectQuery)
        assertThat(resultQuery.sql).isEqualTo(expectedSql)
        assertThat(getArgs(resultQuery)).isEqualTo(arrayOf<Any?>(projectId, moduleId.value))
    }

    @Test
    fun `buildSubjectQuery includes credential join clause when externalCredential is provided`() {
        val credentialValue = "credentialValue"
        val query = SubjectQuery(
            projectId = "projectId",
            externalCredential = credentialValue.asTokenizableEncrypted(),
        )

        val result = RoomEnrolmentRecordQueryBuilder().buildSubjectQuery(query)

        val expectedSql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            INNER JOIN $EXTERNAL_CREDENTIAL_TABLE_NAME C ON S.$SUBJECT_ID_COLUMN = C.$SUBJECT_ID_COLUMN
            WHERE S.$PROJECT_ID_COLUMN = ? AND C.$EXTERNAL_CREDENTIAL_VALUE_COLUMN = ?
            
            """.trimIndent()

        assertThat(result.sql).isEqualTo(expectedSql)
    }

    private fun getArgs(query: SimpleSQLiteQuery): Array<Any?> {
        val argsMap = mutableMapOf<Int, Any?>()
        val program = object : SupportSQLiteProgram {
            override fun bindNull(index: Int) {
                argsMap[index] = null
            }

            override fun bindLong(
                index: Int,
                value: Long,
            ) {
                argsMap[index] = value
            }

            override fun bindDouble(
                index: Int,
                value: Double,
            ) {
                argsMap[index] = value
            }

            override fun bindString(
                index: Int,
                value: String,
            ) {
                argsMap[index] = value
            }

            override fun bindBlob(
                index: Int,
                value: ByteArray,
            ) {
                argsMap[index] = value
            }

            override fun clearBindings() {
                argsMap.clear()
            }

            override fun close() {}
        }

        query.bindTo(program)

        val maxIndex = argsMap.keys.maxOrNull() ?: 0
        return Array(maxIndex) { i -> argsMap[i + 1] }
    }
}
