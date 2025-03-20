package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.PROJECT_ID_COLUMN
import java.util.UUID

@Entity(
    tableName = "DbSubject",
    indices = [
        Index(value = [PROJECT_ID_COLUMN]),
        Index(value = [ATTENDANT_ID_COLUMN]),
        Index(value = [MODULE_ID_COLUMN]),
    ],
)
data class DbSubject(
    @PrimaryKey
    val subjectId: String = UUID.randomUUID().toString(),
    val projectId: String? = "",
    val attendantId: String? = "",
    val moduleId: String? = "",
    val createdAt: Long? = 0,
    val updatedAt: Long? = 0,
    val toSync: Boolean = false,
    val isAttendantIdTokenized: Boolean = false,
    val isModuleIdTokenized: Boolean = false,
) {
    companion object {
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val PROJECT_ID_COLUMN = "projectId"
        const val ATTENDANT_ID_COLUMN = "attendantId"
        const val MODULE_ID_COLUMN = "moduleId"
        const val FORMAT_COLUMN = "format"
    }
}
