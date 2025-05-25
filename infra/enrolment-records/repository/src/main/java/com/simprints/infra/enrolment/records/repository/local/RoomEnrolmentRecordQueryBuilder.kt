package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import javax.inject.Inject
import kotlin.io.println

internal class RoomEnrolmentRecordQueryBuilder @Inject constructor() {
    fun buildSubjectQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val orderBy = if (query.sort) "ORDER BY $SUBJECT_ID_COLUMN ASC" else ""
        val sql =
            """
             SELECT * FROM $TEMPLATE_TABLE_NAME 
            $whereClause
            $orderBy
            """.trimIndent()
        println(sql)
        println("----")
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildCountQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val sql = "SELECT COUNT(DISTINCT $SUBJECT_ID_COLUMN) FROM $TEMPLATE_TABLE_NAME $whereClause"
        println(sql)
        println("----")

        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildBiometricTemplatesQuery(
        query: SubjectQuery,
        range: IntRange,
    ): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val sql =
            """
        SELECT *
        FROM $TEMPLATE_TABLE_NAME         
            $whereClause
            ORDER BY $SUBJECT_ID_COLUMN ASC
            LIMIT ${range.last - range.first} OFFSET ${range.first}        
        """
        println(sql)
        println("----")

        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildWhereClause(query: SubjectQuery): Pair<String, MutableList<Any?>> {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        if (query.fingerprintSampleFormat != null && query.faceSampleFormat != null) {
            throw IllegalArgumentException("Cannot set both fingerprintSampleFormat and faceSampleFormat")
        }
        // to achieve the highest performance, we should not use OR in the where clause
        query.subjectId?.let {
            clauses.add("$SUBJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            clauses.add("$SUBJECT_ID_COLUMN IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.afterSubjectId?.let {
            clauses.add("$SUBJECT_ID_COLUMN > ?")
            args.add(it)
        }
        query.projectId?.let {
            clauses.add("$PROJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.attendantId?.let {
            clauses.add("$ATTENDANT_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.moduleId?.let {
            clauses.add("$MODULE_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.faceSampleFormat?.let {
            clauses.add("$FORMAT_COLUMN = ?")
            args.add(it)
        }
        query.fingerprintSampleFormat?.let {
            clauses.add("$FORMAT_COLUMN = ?")
            args.add(it)
        }

        val whereClauseResult =
            if (clauses.isNotEmpty()) "WHERE ${clauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }
}
