package com.simprints.infra.enrolment.records.store.local.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simprints.core.domain.face.FaceSample

@Entity(
    tableName = "face_samples",
    foreignKeys = [
        ForeignKey(
            entity = DbSubject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE, // ✅ Ensures consistency
        ),
    ],
    indices = [
        Index(value = ["format"]),
        Index(value = ["subjectId"]),

    ],
)
data class DbFaceSample(
    @PrimaryKey
    val id: String,
    val subjectId: String, // Foreign Key reference
    val template: ByteArray,
    val format: String,
)

internal fun DbFaceSample.fromDbToDomain(): FaceSample = FaceSample(
    id = id,
    template = template,
    format = format,
    subjectId = subjectId,
)

internal fun FaceSample.fromDomainToDb(): DbFaceSample = DbFaceSample(
    id = id,
    template = template,
    format = format,
    subjectId = subjectId,
)
