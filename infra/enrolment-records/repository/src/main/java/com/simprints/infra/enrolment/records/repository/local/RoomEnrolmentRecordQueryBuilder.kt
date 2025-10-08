package com.simprints.infra.enrolment.records.repository.local

import androidx.sqlite.db.SimpleSQLiteQuery
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
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ROOM_RECORDS_DB
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class RoomEnrolmentRecordQueryBuilder @Inject constructor() {
    /**
     * Builds a query to select subjects based on the provided [SubjectQuery].
     * The query will be on the `SUBJECT_TABLE_NAME` table and will include filtering criteria
     * Don't set the format in the [SubjectQuery] for this method instead use [buildBiometricTemplatesQuery].
     * @param query The [SubjectQuery] containing the filtering criteria.
     * @return A [SimpleSQLiteQuery] that can be executed against the database.
     */
    fun buildSubjectQuery(query: SubjectQuery): SimpleSQLiteQuery {
        // require format not to be set for subject query and guide to use the buildBiometricTemplatesQuery instead
        require(query.fingerprintSampleFormat == null && query.faceSampleFormat == null) {
            "Cannot set format for subject query, use buildBiometricTemplatesQuery instead"
        }
        val (whereClause, args) = buildWhereClause(query)
        val credentialJoinClause = buildCredentialJoinClause(query)
        val orderByClause = buildOrderByClause(query)
        val sql =
            """
            SELECT * FROM $SUBJECT_TABLE_NAME S
            $credentialJoinClause
            $whereClause
            $orderByClause
            """.trimIndent()
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildCountQuery(query: SubjectQuery): SimpleSQLiteQuery {
        val (whereClause, args) = buildWhereClause(query)
        val specificFormat = query.fingerprintSampleFormat ?: query.faceSampleFormat

        val sql = if (specificFormat != null) {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T" +
                " using(subjectId) $whereClause"
        } else {
            "SELECT COUNT(DISTINCT S.$SUBJECT_ID_COLUMN) FROM $SUBJECT_TABLE_NAME S $whereClause"
        }
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildBiometricTemplatesQuery(
        query: SubjectQuery,
        pageSize: Int,
    ): SimpleSQLiteQuery {
        // require format to be set for biometric templates query
        val format = query.fingerprintSampleFormat ?: query.faceSampleFormat
        require(format != null) {
            "Must set format for biometric templates query, use buildSubjectQuery or buildCountQuery instead"
        }
        val updatedQuery = query.copy(sort = true)
        val (whereClause, args) = buildWhereClause(updatedQuery)
        val orderByClause = buildOrderByClause(updatedQuery)
        val sql =
            """
            SELECT A.*
            FROM $TEMPLATE_TABLE_NAME A
             INNER JOIN (
                SELECT distinct  S.subjectId
                FROM $SUBJECT_TABLE_NAME S  INNER JOIN  $TEMPLATE_TABLE_NAME T
                USING(subjectId)
                $whereClause
                $orderByClause
                LIMIT $pageSize
            ) B USING(subjectId) where A.format ='$format'
            """.trimIndent()
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    fun buildDeleteQuery(query: SubjectQuery): SimpleSQLiteQuery {
        require(query.faceSampleFormat == null && query.fingerprintSampleFormat == null) {
            val errorMsg = "faceSampleFormat and fingerprintSampleFormat are not supported for deletion"
            Simber.i("[delete] $errorMsg", tag = ROOM_RECORDS_DB)
            errorMsg
        }
        val (whereClause, args) = buildWhereClause(
            query,
            subjectAlias = "",
            templateAlias = "",
            credentialAlias = "",
        )
        val sql = "DELETE FROM DbSubject $whereClause"
        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }

    private fun buildWhereClause(
        query: SubjectQuery,
        subjectAlias: String = "S.", // Default alias for subject table, dot included. Empty string for no alias.
        templateAlias: String = "T.", // Default alias for template table, dot included. Empty string for no alias.
        credentialAlias: String = "C.", // Default alias for credentials table, dot included. Empty string for no alias.
    ): Pair<String, List<Any?>> {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        require(!(query.fingerprintSampleFormat != null && query.faceSampleFormat != null)) {
            "Cannot set both fingerprintSampleFormat and faceSampleFormat"
        }
        // to achieve the highest performance, we should not use OR in the where clause
        // subject id params are mutually exclusive, so only one of them will be set at a time
        when {
            query.subjectId != null -> {
                clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN = ?")
                args.add(query.subjectId)
            }

            query.subjectIds?.isNotEmpty() == true -> {
                clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN IN (${query.subjectIds.joinToString(",") { "?" }})")
                args.addAll(query.subjectIds)
            }

            query.afterSubjectId != null -> {
                clauses.add("${subjectAlias}$SUBJECT_ID_COLUMN > ?")
                args.add(query.afterSubjectId)
            }
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

        query.externalCredential?.let {
            clauses.add("${credentialAlias}$EXTERNAL_CREDENTIAL_VALUE_COLUMN = ?")
            args.add(query.externalCredential)
        }

        var whereClauseResult = if (clauses.isNotEmpty()) "WHERE ${clauses.joinToString(" AND ")}" else ""
        return Pair(whereClauseResult, args)
    }

    private fun buildCredentialJoinClause(
        query: SubjectQuery,
        subjectAlias: String = "S",
        credentialAlias: String = "C",
    ): String = if (query.externalCredential != null) {
        "INNER JOIN $EXTERNAL_CREDENTIAL_TABLE_NAME $credentialAlias ON $subjectAlias.$SUBJECT_ID_COLUMN = $credentialAlias.$SUBJECT_ID_COLUMN"
    } else {
        ""
    }

    private fun buildOrderByClause(
        query: SubjectQuery,
        subjectAlias: String = "S.",
    ) = if (query.sort) {
        "ORDER BY $subjectAlias$SUBJECT_ID_COLUMN ASC"
    } else {
        ""
    }
}
