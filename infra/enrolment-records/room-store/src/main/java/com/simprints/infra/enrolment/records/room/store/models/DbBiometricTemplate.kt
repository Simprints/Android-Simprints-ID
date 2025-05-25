package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.ATTENDANT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.MODULE_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.PROJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME

@Entity(
    tableName = TEMPLATE_TABLE_NAME,
    indices = [
        // For queries filtering/ordering by subjectId primarily
        Index(value = [SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND attendantId = ? ... ORDER BY subjectId
        // Equality predicates (projectId, attendantId) first, then range/sort (subjectId)
        Index(value = [PROJECT_ID_COLUMN, ATTENDANT_ID_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND attendantId = ?  AND format = ? ORDER BY subjectId
        Index(value = [PROJECT_ID_COLUMN, ATTENDANT_ID_COLUMN, FORMAT_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND moduleId = ? ... ORDER BY subjectId
        // Equality predicates (projectId, moduleId) first, then range/sort (subjectId)
        Index(value = [PROJECT_ID_COLUMN, MODULE_ID_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND moduleId = ?  AND format = ? ORDER BY subjectId
        Index(value = [PROJECT_ID_COLUMN, MODULE_ID_COLUMN, FORMAT_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND format = ? ORDER BY subjectId
        // Also helps: WHERE projectId = ? AND format = ?
        // Also helps: COUNT(DISTINCT subjectId) WHERE projectId = ? AND format = ?
        Index(value = [PROJECT_ID_COLUMN, FORMAT_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE format = ? ORDER BY subjectId
        // Also helps: WHERE format = ?
        // Also helps: COUNT(DISTINCT subjectId) WHERE format = ?
        Index(value = [FORMAT_COLUMN, SUBJECT_ID_COLUMN]),

        // For: COUNT(DISTINCT subjectId) WHERE moduleId = ?
        Index(value = [MODULE_ID_COLUMN, SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? ORDER BY subjectId
        // Also helps: WHERE projectId = ?
        // Also helps: COUNT(DISTINCT subjectId) WHERE projectId = ?
        Index(value = [PROJECT_ID_COLUMN, SUBJECT_ID_COLUMN]),

        // For: COUNT(DISTINCT subjectId) WHERE attendantId = ?
        Index(value = [ATTENDANT_ID_COLUMN, SUBJECT_ID_COLUMN]),

    ],
)
@Suppress("ArrayInDataClass")
data class DbBiometricTemplate(
    @PrimaryKey
    val uuid: String = "",
    val subjectId: String = "",
    val identifier: Int? = null, // e.g the finger number or other identifier for the biometric
    val templateData: ByteArray = byteArrayOf(),
    val format: String = "",
    val referenceId: String = "",
    val modality: Int = Modality.FINGERPRINT.id,
    val projectId: String = "",
    val attendantId: String = "",
    val moduleId: String = "",
    val createdAt: Long? = 0,
    val updatedAt: Long? = 0,
) {
    companion object {
        const val TEMPLATE_TABLE_NAME = "DbBiometricTemplate"
        const val FORMAT_COLUMN = "format"
        const val UUID_COLUMN = "uuid"
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val IDENTIFIER_COLUMN = "identifier"
        const val REFERENCE_ID_COLUMN = "referenceId"
        const val MODALITY_COLUMN = "modality"
        const val PROJECT_ID_COLUMN = "projectId"
        const val ATTENDANT_ID_COLUMN = "attendantId"
        const val MODULE_ID_COLUMN = "moduleId"
        const val CREATED_AT_COLUMN = "createdAt"
        const val UPDATED_AT_COLUMN = "updatedAt"
        const val TEMPLATE_DATA_COLUMN = "templateData"
    }
}
