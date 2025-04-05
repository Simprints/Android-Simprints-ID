package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = "DbFaceSample",
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
data class DbFaceSample(
    //  Auto-incrementing key for pagination
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0, // This field is automatically assigned by Room
    val uuid: String = "",
    val subjectId: String = "",
    val template: ByteArray = byteArrayOf(),
    val format: String = "",
    val referenceId: String = "",
)
