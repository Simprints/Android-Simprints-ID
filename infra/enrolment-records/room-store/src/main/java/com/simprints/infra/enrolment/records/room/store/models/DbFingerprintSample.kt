package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = "DbFingerprintSample",
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = [SUBJECT_ID_COLUMN],
            childColumns = [SUBJECT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = [FORMAT_COLUMN]),
        Index(value = [SUBJECT_ID_COLUMN]),

    ],
)
@Suppress("ArrayInDataClass")
data class DbFingerprintSample(
    @PrimaryKey
    val id: String,
    val subjectId: String,
    val fingerIdentifier: Int,
    val template: ByteArray,
    val templateQualityScore: Int,
    val format: String,
    val referenceId: String,
)
