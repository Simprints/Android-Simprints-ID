package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbCommCareCase.Companion.COMMCARE_CASE_TABLE_NAME

/**
 * Represents a CommCare case sync tracking entry in the local database.
 * Stores the relationship between CommCare caseId, Simprints subjectId and last_modified timestamp.
 */
@Entity(
    tableName = COMMCARE_CASE_TABLE_NAME,
    indices = [
        Index(value = [DbCommCareCase.CASE_ID_COLUMN]),
        Index(value = [DbCommCareCase.SUBJECT_ID_COLUMN]),
    ],
)
data class DbCommCareCase(
    @PrimaryKey
    val caseId: String,
    val subjectId: String,
    val lastModified: Long,
) {
    companion object {
        const val COMMCARE_CASE_TABLE_NAME = "DbCommCareCase"
        const val CASE_ID_COLUMN = "caseId"
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val LAST_MODIFIED_COLUMN = "lastModified"
    }
}