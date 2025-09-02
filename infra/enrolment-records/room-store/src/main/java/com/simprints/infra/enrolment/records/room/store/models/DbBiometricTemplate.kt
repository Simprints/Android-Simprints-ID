package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.TEMPLATE_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = TEMPLATE_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = [SUBJECT_ID_COLUMN],
            childColumns = [SUBJECT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = [FORMAT_COLUMN, SUBJECT_ID_COLUMN]),
        Index(value = [SUBJECT_ID_COLUMN]),
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
    val modality: Int = DbModality.FINGERPRINT.id,
) {
    companion object {
        const val TEMPLATE_TABLE_NAME = "DbBiometricTemplate"
        const val FORMAT_COLUMN = "format"
    }
}

