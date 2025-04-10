package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_TABLE_NAME
import jakarta.inject.Inject

internal class RoomEnrolmentRecordQueryBuilder @Inject constructor() {
    fun buildSubjectQuery(query: SubjectQuery): SimpleSQLiteQuery {
        // require format not to be set for subject query and guid to use the buildBiometricTemplatesQuery instead
        require(query.fingerprintSampleFormat == null && query.faceSampleFormat == null) {
            "Cannot set format for subject query, use buildBiometricTemplatesQuery instead"
        }
        val (whereClause, args) = buildWhereAndOrderByClause(query)
        val sql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            $whereClause
            """.trimIndent()
        println(sql)
        println("----")
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildCountQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereAndOrderByClause(query)
        val specificFormat = query.fingerprintSampleFormat ?: query.faceSampleFormat

        val sql = if (specificFormat != null) {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using(subjectId) $whereClause "
        } else {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S $whereClause"
        }
        println(sql)
        println("----")
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildBiometricTemplatesQuery(
        query: SubjectQuery,
        pageSize: Int,
        lastSeenSubjectId: String? = null,
    ): SimpleSQLiteQuery {
        // require format to be set for biometric templates query
        val format = query.fingerprintSampleFormat ?: query.faceSampleFormat
        require(format != null) {
            "Must set format for biometric templates query, use buildSubjectQuery or buildCountQuery instead"
        }
        val updatedQuery = query.copy(afterSubjectId = lastSeenSubjectId, sort = true)
        val (whereClause, args) = buildWhereAndOrderByClause(updatedQuery)
        val sql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.subjectId
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING(subjectId)
                $whereClause
                LIMIT $pageSize
            ) B USING(subjectId) where A.format ='$format'
            """.trimIndent()
        println(sql)
        println("----")
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildWhereAndOrderByClause(
        query: SubjectQuery,
        subjectAlias: String = "S.", // Default alias for subject table, dot included. Empty string for no alias.
        templateAlias: String = "T.", // Default alias for template table, dot included. Empty string for no alias.
    ): Pair<String, MutableList<Any?>> {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        if (query.fingerprintSampleFormat != null && query.faceSampleFormat != null) {
            throw IllegalArgumentException("Cannot set both fingerprintSampleFormat and faceSampleFormat")
        }
        // to achieve the highest performance, we should not use OR in the where clause
        query.subjectId?.let {
            clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.afterSubjectId?.let {
            clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN > ?")
            args.add(it)
        }
        query.projectId?.let {
            clauses.add("${subjectAlias}$PROJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.attendantId?.let {
            clauses.add("${subjectAlias}$ATTENDANT_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.moduleId?.let {
            clauses.add("${subjectAlias}$MODULE_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.faceSampleFormat?.let {
            clauses.add("${templateAlias}$FORMAT_COLUMN = ?")
            args.add(it)
        }
        query.fingerprintSampleFormat?.let {
            clauses.add("${templateAlias}$FORMAT_COLUMN = ?")
            args.add(it)
        }

        var whereClauseResult = if (clauses.isNotEmpty()) "WHERE ${clauses.joinToString(" AND ")}" else ""
        if (query.sort) {
            whereClauseResult += " ORDER BY ${subjectAlias}$SUBJECT_ID_COLUMN ASC"
        }
        return Pair(whereClauseResult, args)
    }
}
