package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate.Companion.UUID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = "DbBiometricTemplate",
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = [SUBJECT_ID_COLUMN],
            childColumns = [SUBJECT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = [SUBJECT_ID_COLUMN, FORMAT_COLUMN]),
        Index(value = [SUBJECT_ID_COLUMN]),
        Index(value = [UUID_COLUMN], unique = true),
    ],
)
@Suppress("ArrayInDataClass")
data class DbBiometricTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: String = "",
    val subjectId: String = "",
    val fingerIdentifier: Int? = null, // for fingerprint samples only
    val templateData: ByteArray = byteArrayOf(),
    val format: String = "",
    val referenceId: String = "",
) {
    companion object {
        const val UUID_COLUMN = "uuid"
        const val FORMAT_COLUMN = "format"
    }
}
