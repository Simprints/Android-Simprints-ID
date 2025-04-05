package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics
import java.util.Date
import com.simprints.infra.enrolment.records.room.store.models.DbSubject as RoomSubject

internal fun SubjectBiometrics.toDomain(): Subject {
    val attendantId =
        if (subject.isAttendantIdTokenized) subject.attendantId.asTokenizableEncrypted() else subject.attendantId.asTokenizableRaw()
    val moduleId =
        if (subject.isModuleIdTokenized) subject.moduleId.asTokenizableEncrypted() else subject.moduleId.asTokenizableRaw()

    return Subject(
        subjectId = subject.subjectId.toString(),
        projectId = subject.projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = subject.createdAt?.toDate(),
        updatedAt = subject.updatedAt?.toDate(),
        fingerprintSamples = fingerprintSamples.map { it.toDomain() },
        faceSamples = faceSamples.map { it.toDomain() },
    )
}

internal fun Subject.toRoomDb(): RoomSubject = RoomSubject(
    subjectId = subjectId,
    projectId = projectId,
    attendantId = attendantId.value,
    moduleId = moduleId.value,
    createdAt = createdAt?.time,
    updatedAt = updatedAt?.time,
    isModuleIdTokenized = moduleId.isTokenized(),
    isAttendantIdTokenized = attendantId.isTokenized(),
)

fun Long.toDate() = Date(this)
