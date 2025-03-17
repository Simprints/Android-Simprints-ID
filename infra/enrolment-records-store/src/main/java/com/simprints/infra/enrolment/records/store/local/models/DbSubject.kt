package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.realm.models.DbSubject
import java.util.Date

internal fun DbSubject.fromDbToDomain(): Subject {
    val attendantId =
        if (isAttendantIdTokenized) attendantId.asTokenizableEncrypted() else attendantId.asTokenizableRaw()
    val moduleId =
        if (isModuleIdTokenized) moduleId.asTokenizableEncrypted() else moduleId.asTokenizableRaw()

    return Subject(
        subjectId = subjectUuid.toString(),
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt?.toDate(),
        updatedAt = updatedAt?.toDate(),
        toSync = toSync,
    )
}

internal fun Subject.fromDomainToDb(): DbSubject = DbSubject().also { subject ->
    subject.subjectUuid = subjectId
    subject.projectId = projectId
    subject.attendantId = attendantId.value
    subject.moduleId = moduleId.value
    subject.createdAt = createdAt?.time
    subject.updatedAt = updatedAt?.time
    subject.toSync = toSync
//    subject.fingerprintSamples.addAll(
//        this.fingerprintSamples.map { it.fromDomainToDb() },
//    )

//    subject.faceSamples.addAll(
//        this.faceSamples.map { it.fromDomainToDb() },
//    )
    subject.isModuleIdTokenized = moduleId.isTokenized()
    subject.isAttendantIdTokenized = attendantId.isTokenized()
}

fun Long.toDate(): Date = Date(this)
