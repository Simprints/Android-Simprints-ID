package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_TABLE_NAME
import java.util.UUID

/**
 * Represents a Subject entry in the local database.
 *
 * Indexes are crucial for query performance, especially for large datasets.
 * The indexes defined here are chosen based on the common query patterns observed
 * in SubjectDao, particularly the frequent use of projectId, filtering by either
 * moduleId or attendantId, and the ordering requirements of the loadSamples query.
 *
 * Note: Indexes speed up read operations (SELECT) but can slightly slow down
 * write operations (INSERT, UPDATE, DELETE) and consume additional storage space.
 */
@Entity(
    tableName = SUBJECT_TABLE_NAME,
    indices = [
        Index(value = [DbSubject.PROJECT_ID_COLUMN, DbSubject.SUBJECT_ID_COLUMN]),
        Index(value = [DbSubject.PROJECT_ID_COLUMN, DbSubject.MODULE_ID_COLUMN, DbSubject.SUBJECT_ID_COLUMN]),
        Index(value = [DbSubject.PROJECT_ID_COLUMN, DbSubject.ATTENDANT_ID_COLUMN, DbSubject.SUBJECT_ID_COLUMN]),
    ],
)
data class DbSubject(
    @PrimaryKey
    val subjectId: String = UUID.randomUUID().toString(),
    val projectId: String = "",
    val attendantId: String = "",
    val moduleId: String = "",
    val createdAt: Long? = 0,
    val updatedAt: Long? = 0,
) {
    /**
     * Companion object holding constants for column names.
     */
    companion object {
        const val SUBJECT_TABLE_NAME = "DbSubject"
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val PROJECT_ID_COLUMN = "projectId"
        const val ATTENDANT_ID_COLUMN = "attendantId"
        const val MODULE_ID_COLUMN = "moduleId"
    }
}
