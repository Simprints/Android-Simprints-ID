package com.simprints.infra.enrolment.records.store.local.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import java.util.Date
import java.util.UUID
import com.simprints.infra.enrolment.records.store.domain.models.Subject as SubjectDomain

@Entity(
    tableName = "subjects",
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["attendantId"]),
        Index(value = ["moduleId"]),
    ],
)
data class DbSubject(
    @PrimaryKey
    val subjectId: String = UUID.randomUUID().toString(),
    val projectId: String? = "",
    val attendantId: String? = "",
    val moduleId: String? = "",
    val createdAt: Long? = 0,
    val updatedAt: Long? = 0,
    val toSync: Boolean = false,
    val isAttendantIdTokenized: Boolean = false,
    val isModuleIdTokenized: Boolean = false,
)

data class Subject(
    @Embedded val subject: DbSubject,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "subjectId",
    )
    val fingerprintSamples: List<DbFingerprintSample>,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "subjectId",
    )
    val faceSamples: List<DbFaceSample>,
)

fun Subject.toDomain(): SubjectDomain {
    val attendantId =
        if (subject.isAttendantIdTokenized) subject.attendantId?.asTokenizableEncrypted() else subject.attendantId?.asTokenizableRaw()
    val moduleId =
        if (subject.isModuleIdTokenized) subject.moduleId?.asTokenizableEncrypted() else subject.moduleId?.asTokenizableRaw()

    return SubjectDomain(
        subjectId = subject.subjectId.toString(),
        projectId = subject.projectId!!,
        attendantId = attendantId!!,
        moduleId = moduleId!!,
        createdAt = subject.createdAt?.toDate(),
        updatedAt = subject.updatedAt?.toDate(),
        fingerprintSamples = fingerprintSamples.map { it.fromDbToDomain() },
        faceSamples = faceSamples.map { it.fromDbToDomain() },
        toSync = subject.toSync,
    )
}

fun Long.toDate() = Date(this)
