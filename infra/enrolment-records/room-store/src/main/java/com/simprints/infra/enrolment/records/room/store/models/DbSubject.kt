package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["projectId", "subjectId"]),
        Index(value = ["projectId", "moduleId", "subjectId"]),
        Index(value = ["projectId", "attendantId", "subjectId"]),
        Index(value = [ "moduleId"]),
    ],
)
data class DbSubject(
    @PrimaryKey val subjectId: String,
    val projectId: String,
    val moduleId: String?,
    val attendantId: String?,
    val createdAt: Long? = 0,
    val updatedAt: Long? = 0,
) {
    companion object {
        const val SUBJECT_TABLE_NAME = "DbSubject"
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val PROJECT_ID_COLUMN = "projectId"
        const val MODULE_ID_COLUMN = "moduleId"
        const val ATTENDANT_ID_COLUMN = "attendantId"
        const val CREATED_AT_COLUMN = "createdAt"
        const val UPDATED_AT_COLUMN = "updatedAt"
    }
}
