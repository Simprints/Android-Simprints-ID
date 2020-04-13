package com.simprints.id.data.db.person.domain.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordCreation
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordDeletion
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordMove
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordOperation


sealed class EnrolmentRecordOperation(val type: EnrolmentRecordOperationType)

class EnrolmentRecordCreation(val subjectId: String,
                              val projectId: String,
                              val moduleId: String,
                              val attendantId: String,
                              val biometricReferences: List<BiometricReference>) : EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordCreation)

class EnrolmentRecordDeletion(val subjectId: String,
                              val projectId: String,
                              val moduleId: String,
                              val attendantId: String) : EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordDeletion)

class EnrolmentRecordMove(val enrolmentRecordCreation: EnrolmentRecordCreation,
                          val enrolmentRecordDeletion: EnrolmentRecordDeletion) : EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordMove)

enum class EnrolmentRecordOperationType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}

fun ApiEnrolmentRecordOperation.toDomainEnrolmentRecordOperation() = when(this) {
    is ApiEnrolmentRecordCreation -> this.fromApiToDomain()
    is ApiEnrolmentRecordDeletion -> this.fromApiToDomain()
    is ApiEnrolmentRecordMove -> this.fromApiToDomain()
}

fun ApiEnrolmentRecordCreation.fromApiToDomain() =
    EnrolmentRecordCreation(subjectId, projectId, moduleId, attendantId,
        biometricReferences.map { it.fromApiToDomain() })

fun ApiEnrolmentRecordDeletion.fromApiToDomain() =
    EnrolmentRecordDeletion(subjectId, projectId, moduleId, attendantId)

fun ApiEnrolmentRecordMove.fromApiToDomain() =
    EnrolmentRecordMove(enrolmentRecordCreation.fromApiToDomain(), enrolmentRecordDeletion.fromApiToDomain())
