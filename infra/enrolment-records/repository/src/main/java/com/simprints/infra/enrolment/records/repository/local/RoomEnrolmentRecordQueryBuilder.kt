package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_TABLE_NAME
import javax.inject.Inject

internal class RoomEnrolmentRecordQueryBuilder @Inject constructor() {
    fun buildSubjectQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val sql =
            """
             SELECT * FROM $TEMPLATE_TABLE_NAME 
            $whereClause
            """.trimIndent()
        println(sql)
        println("----")
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildCountQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)

        val sql = if (query.faceSampleFormat != null || query.fingerprintSampleFormat != null) {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S JOIN  $TEMPLATE_TABLE_NAME T" +
                " on T.subjectId = S.subjectId $whereClause "
        } else {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  $whereClause "
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
        val updatedQuery = query.copy(afterSubjectId = lastSeenSubjectId, sort = true)
        val (whereClause, args) = buildWhereClause(updatedQuery)
        val sql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
            JOIN (
                SELECT distinct  S.subjectId 
                FROM $SUBJECT_TABLE_NAME S JOIN  $TEMPLATE_TABLE_NAME T
                ON S.subjectId =  T.subjectId
                $whereClause
                LIMIT $pageSize
            ) B ON A.subjectId = B.subjectId
            """.trimIndent()
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
            clauses.add("S.$SUBJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.subjectIds?.takeIf { it.isNotEmpty() }?.let {
            clauses.add("S.$SUBJECT_ID_COLUMN IN (${it.joinToString(",") { "?" }})")
            args.addAll(it)
        }
        query.afterSubjectId?.let {
            clauses.add("S.$SUBJECT_ID_COLUMN > ?")
            args.add(it)
        }
        query.projectId?.let {
            clauses.add("S.$PROJECT_ID_COLUMN = ?")
            args.add(it)
        }
        query.attendantId?.let {
            clauses.add("S.$ATTENDANT_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.moduleId?.let {
            clauses.add("S.$MODULE_ID_COLUMN = ?")
            args.add(it.value)
        }
        query.faceSampleFormat?.let {
            clauses.add("T.$FORMAT_COLUMN = ?")
            args.add(it)
        }
        query.fingerprintSampleFormat?.let {
            clauses.add("T.$FORMAT_COLUMN = ?")
            args.add(it)
        }

        var whereClauseResult =
            if (clauses.isNotEmpty()) "WHERE ${clauses.joinToString(" AND ")}" else ""
        whereClauseResult += if (query.sort) "ORDER BY S.$SUBJECT_ID_COLUMN ASC" else ""
        return Pair(whereClauseResult, args)
    }
}
