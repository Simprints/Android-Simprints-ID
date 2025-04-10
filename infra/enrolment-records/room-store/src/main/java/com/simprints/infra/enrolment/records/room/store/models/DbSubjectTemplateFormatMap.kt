package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = "DbSubjectTemplateFormatMap",
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
    ],
)
data class DbSubjectTemplateFormatMap(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: String,
    val format: String,
)
