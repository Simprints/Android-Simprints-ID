package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.CREATED_AT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap.Companion.FORMAT_MAP_TABLE_NAME
import jakarta.inject.Inject

internal class RoomEnrolmentRecordQueryBuilder @Inject constructor() {
    fun buildSubjectQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val orderBy = if (query.sort) "ORDER BY s.$SUBJECT_ID_COLUMN ASC" else ""
        val sql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME s
            LEFT JOIN $TEMPLATE_TABLE_NAME b ON s.$SUBJECT_ID_COLUMN = b.$SUBJECT_ID_COLUMN 
            $whereClause
            GROUP BY s.$SUBJECT_ID_COLUMN 
            $orderBy
            """.trimIndent()
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildCountQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val args = mutableListOf<Any?>()
        val whereClauses = mutableListOf<String>()
        val sqlBuilder = StringBuilder()

        val specificFormat = query.fingerprintSampleFormat ?: query.faceSampleFormat

        if (specificFormat != null) {
            sqlBuilder.append("SELECT COUNT(DISTINCT s.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME s ")
            sqlBuilder.append("INNER JOIN $FORMAT_MAP_TABLE_NAME m ON s.$SUBJECT_ID_COLUMN = m.$SUBJECT_ID_COLUMN ")
            appendCommonSubjectConditions(query, whereClauses, args, "s.")
            whereClauses.add("m.$FORMAT_COLUMN = ?")
            args.add(specificFormat)
        } else {
            sqlBuilder.append("SELECT COUNT(s.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME s")
            appendCommonSubjectConditions(query, whereClauses, args, "s.")
        }

        if (whereClauses.isNotEmpty()) {
            sqlBuilder.append(" WHERE ").append(whereClauses.joinToString(" AND "))
        }
        val resultQuery = sqlBuilder.toString()
        return SimpleSQLiteQuery(resultQuery, args.toTypedArray())
    }

    fun buildBiometricTemplatesQuery(
        query: SubjectQuery,
        range: IntRange,
    ): SimpleSQLiteQuery {
        val (whereClause, args) = buildBiometricTemplatesWhereClause(query)
        val format = query.fingerprintSampleFormat ?: query.faceSampleFormat
            ?: throw IllegalArgumentException("Either fingerprintSampleFormat or faceSampleFormat must be provided")
        val sql =
            """
        SELECT b.*
        FROM $TEMPLATE_TABLE_NAME  b
        JOIN (
            SELECT s.$SUBJECT_ID_COLUMN 
            FROM $SUBJECT_TABLE_NAME s
            INNER JOIN $FORMAT_MAP_TABLE_NAME m ON s.$SUBJECT_ID_COLUMN = m.$SUBJECT_ID_COLUMN 
            where m.$FORMAT_COLUMN = "$format"
            $whereClause
            ORDER BY s.$CREATED_AT_COLUMN
            LIMIT ${range.last - range.first} OFFSET ${range.first}
        ) AS filterSubjects
        ON b.$SUBJECT_ID_COLUMN = filterSubjects.$SUBJECT_ID_COLUMN AND b.$FORMAT_COLUMN = "$format"
        """
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildWhereClauseForDelete(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        appendCommonSubjectConditions(query, whereClauses, args, "")
        val whereClauseResult =
            if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun buildWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()

        appendCommonSubjectConditions(query, whereClauses, args, "s.")

        query.fingerprintSampleFormat?.let {
            whereClauses.add("b.$FORMAT_COLUMN = ?")
            args.add(it)
        }
        query.faceSampleFormat?.let {
            whereClauses.add("b.$FORMAT_COLUMN = ?")
            args.add(it)
        }

        val whereClauseResult =
            if (whereClauses.isNotEmpty()) "WHERE ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun buildBiometricTemplatesWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        appendCommonSubjectConditions(query, whereClauses, args, "s.")
        val whereClauseResult =
            if (whereClauses.isNotEmpty()) "AND ${whereClauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun appendCommonSubjectConditions(
        query: SubjectQuery,
        clauses: MutableList<String>,
        args: MutableList<Any?>,
        aliasPrefix: String,
    ) {
        query.projectId?.let {
            clauses.add("${aliasPrefix}$PROJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.subjectId?.let {
            clauses.add("$aliasPrefix$SUBJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            clauses.add("$aliasPrefix$SUBJECT_ID_COLUMN IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.attendantId?.let {
            clauses.add("${aliasPrefix}$ATTENDANT_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.moduleId?.let {
            clauses.add("${aliasPrefix}$MODULE_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.afterSubjectId?.let {
            clauses.add("$aliasPrefix$SUBJECT_ID_COLUMN > ?")
            args.add(it)
        }
    }
}
