package com.simprints.infra.enrolment.records.room.store.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential.Companion.EXTERNAL_CREDENTIAL_TABLE_NAME
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential.Companion.EXTERNAL_CREDENTIAL_VALUE_COLUMN
import com.simprints.infra.enrolment.records.room.store.models.DbSubject.Companion.SUBJECT_ID_COLUMN

@Entity(
    tableName = EXTERNAL_CREDENTIAL_TABLE_NAME,
    primaryKeys = [EXTERNAL_CREDENTIAL_VALUE_COLUMN, SUBJECT_ID_COLUMN],
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = [SUBJECT_ID_COLUMN],
            childColumns = [SUBJECT_ID_COLUMN],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class DbExternalCredential(
    // The ID is only used by BFSID for analytics. The primary key should be a composite of value+subjectId
    val id: String,
    @ColumnInfo(name = EXTERNAL_CREDENTIAL_VALUE_COLUMN)
    val value: String,
    @ColumnInfo(name = SUBJECT_ID_COLUMN)
    val subjectId: String,
    val type: String,
) {
    companion object {
        const val EXTERNAL_CREDENTIAL_VALUE_COLUMN = "value"
        const val EXTERNAL_CREDENTIAL_TABLE_NAME = "DbExternalCredential"
    }
}
