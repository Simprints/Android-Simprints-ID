package com.simprints.infra.enrolment.records.repository.local

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import org.junit.Test

class RoomEnrolmentRecordQueryBuilderTest {
    private val builder = RoomEnrolmentRecordQueryBuilder()

    @Test
    fun `buildSubjectQuery with empty query returns base query`() {
        val query = SubjectQuery()
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("SELECT * FROM DbBiometricTemplate")
        assertThat(result.argCount).isEqualTo(0)
    }

    @Test
    fun `buildSubjectQuery with sort true adds ORDER BY`() {
        val query = SubjectQuery(sort = true)
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("ORDER BY subjectId ASC")
    }

    @Test
    fun `buildSubjectQuery with fingerprintSampleFormat adds format filter`() {
        val query = SubjectQuery(fingerprintSampleFormat = "formatA")
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("format = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildSubjectQuery with faceSampleFormat adds format filter`() {
        val query = SubjectQuery(faceSampleFormat = "faceFormat")
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("format = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery with no format returns count query`() {
        val query = SubjectQuery(projectId = "p1")
        val result = builder.buildCountQuery(query)
        assertThat(result.sql).contains("SELECT COUNT(DISTINCT subjectId) FROM DbBiometricTemplate")
        assertThat(result.sql).contains("projectId = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery with fingerprintSampleFormat returns count distinct query`() {
        val query = SubjectQuery(fingerprintSampleFormat = "formatA")
        val result = builder.buildCountQuery(query)
        assertThat(result.sql).contains("SELECT COUNT(DISTINCT subjectId)")
        assertThat(result.sql).contains("format = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildCountQuery with faceSampleFormat returns count distinct query`() {
        val query = SubjectQuery(faceSampleFormat = "faceFormat")
        val result = builder.buildCountQuery(query)
        assertThat(result.sql).contains("SELECT COUNT(DISTINCT subjectId)")
        assertThat(result.sql).contains("format = ?")
        assertThat(result.argCount).isEqualTo(1)
    }

    @Test
    fun `buildBiometricTemplatesQuery throws if no format provided`() {
        val query = SubjectQuery()
        try {
            builder.buildBiometricTemplatesQuery(query, 10, "0")
        } catch (e: IllegalArgumentException) {
            assertThat(e).hasMessageThat().contains("Either fingerprintSampleFormat or faceSampleFormat must be provided")
        }
    }

    @Test
    fun `buildBiometricTemplatesQuery with fingerprintSampleFormat builds correct query`() {
        val query = SubjectQuery(fingerprintSampleFormat = "formatA", projectId = "p1", attendantId = "a1".asTokenizableEncrypted())
        val result = builder.buildBiometricTemplatesQuery(query, 10, null)
        assertThat(result.sql).contains("format = ?")
        assertThat(result.sql).contains("LIMIT 10")
        assertThat(result.argCount).isEqualTo(3)
    }

    @Test
    fun `buildBiometricTemplatesQuery with faceSampleFormat builds correct query`() {
        val query = SubjectQuery(faceSampleFormat = "faceFormat", projectId = "p1", moduleId = "m1".asTokenizableEncrypted())
        val result = builder.buildBiometricTemplatesQuery(query, 15, null)
        assertThat(result.sql).contains("format = ?")
        assertThat(result.sql).contains("LIMIT 15")
        assertThat(result.argCount).isEqualTo(3)
    }

    @Test
    fun `buildWhereClauseForDelete with no filters returns empty where`() {
        val query = SubjectQuery()
        val (where, args) = builder.buildWhereClause(query)
        assertThat(where).isEmpty()
        assertThat(args).isEmpty()
    }

    @Test
    fun `buildWhereClauseForDelete with filters returns correct where and args`() {
        val query = SubjectQuery(projectId = "p1", subjectId = "s1")
        val (where, args) = builder.buildWhereClause(query)
        assertThat(where).contains("WHERE")
        assertThat(where).contains("projectId = ?")
        assertThat(where).contains("subjectId = ?")
        assertThat(args.size).isEqualTo(2)
    }

    @Test
    fun `buildSubjectQuery with subjectIds in list builds IN clause`() {
        val query = SubjectQuery(subjectIds = listOf("id1", "id2", "id3"))
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("subjectId IN (?,?,?)")
        assertThat(result.argCount).isEqualTo(3)
    }

    @Test
    fun `buildSubjectQuery with afterSubjectId builds correct where`() {
        val query = SubjectQuery(afterSubjectId = "afterId")
        val result = builder.buildSubjectQuery(query)
        assertThat(result.sql).contains("subjectId > ?")
        assertThat(result.argCount).isEqualTo(1)
    }
}
