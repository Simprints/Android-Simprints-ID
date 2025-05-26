package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME

@Entity(
    tableName = TEMPLATE_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // For queries filtering/ordering by subjectId primarily
        Index(value = [SUBJECT_ID_COLUMN]),

        // For: WHERE projectId = ? AND attendantId = ?  AND format = ? ORDER BY subjectId
        Index(value = [ FORMAT_COLUMN, SUBJECT_ID_COLUMN]),

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
) {
    companion object {
        const val TEMPLATE_TABLE_NAME = "DbBiometricTemplate"
        const val FORMAT_COLUMN = "format"
        const val SUBJECT_ID_COLUMN = "subjectId"
        const val PROJECT_ID_COLUMN = "projectId"
        const val ATTENDANT_ID_COLUMN = "attendantId"
        const val MODULE_ID_COLUMN = "moduleId"
    }
}
