package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperationType
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordOperationType.*
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreation as DomainEnrolmentRecordCreation
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordDeletion as DomainEnrolmentRecordDeletion
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordMove as DomainEnrolmentRecordMove

sealed class ApiEnrolmentRecordOperation(val type: ApiEnrolmentRecordOperationType)

class ApiEnrolmentRecordCreation(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String,
                                 val biometricReferences: List<ApiBiometricReference>): ApiEnrolmentRecordOperation(EnrolmentRecordCreation)

class ApiEnrolmentRecordDeletion(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String): ApiEnrolmentRecordOperation(EnrolmentRecordDeletion)

class ApiEnrolmentRecordMove(val enrolmentRecordCreation: ApiEnrolmentRecordCreation,
                             val enrolmentRecordDeletion: ApiEnrolmentRecordDeletion): ApiEnrolmentRecordOperation(EnrolmentRecordMove)

enum class ApiEnrolmentRecordOperationType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}

fun DomainEnrolmentRecordCreation.fromDomainToApi() =
    ApiEnrolmentRecordCreation(subjectId, projectId, moduleId, attendantId, biometricReferences.map { it.fromDomainToApi() })

fun DomainEnrolmentRecordDeletion.fromDomainToApi() =
    ApiEnrolmentRecordDeletion(subjectId, projectId, moduleId, attendantId)

fun DomainEnrolmentRecordMove.fromDomainToApi() =
    ApiEnrolmentRecordMove(enrolmentRecordCreation.fromDomainToApi(), enrolmentRecordDeletion.fromDomainToApi())

fun EnrolmentRecordOperationType.fromDomainToApi() = when (this) {
    EnrolmentRecordOperationType.EnrolmentRecordCreation -> EnrolmentRecordCreation
    EnrolmentRecordOperationType.EnrolmentRecordDeletion -> EnrolmentRecordDeletion
    EnrolmentRecordOperationType.EnrolmentRecordMove -> EnrolmentRecordMove
}
