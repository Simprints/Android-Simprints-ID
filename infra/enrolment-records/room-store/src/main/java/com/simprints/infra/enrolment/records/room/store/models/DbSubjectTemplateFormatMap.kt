package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.Entity
import androidx.room.ForeignKey
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap.Companion.FORMAT_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubjectTemplateFormatMap.Companion.FORMAT_MAP_TABLE_NAME

@Entity(
    tableName = FORMAT_MAP_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = [SUBJECT_ID_COLUMN],
            childColumns = [SUBJECT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    primaryKeys = [SUBJECT_ID_COLUMN, FORMAT_COLUMN],
)
data class DbSubjectTemplateFormatMap(
    val subjectId: String,
    val format: String,
) {
    companion object {
        const val FORMAT_MAP_TABLE_NAME = "DbSubjectTemplateFormatMap"
        const val FORMAT_COLUMN = "format"
    }
}
