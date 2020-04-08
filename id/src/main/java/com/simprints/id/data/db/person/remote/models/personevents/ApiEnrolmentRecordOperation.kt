package com.simprints.id.data.db.person.remote.models.personevents

import androidx.annotation.Keep
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperation
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordOperationType
import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordOperationType.*
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordCreation as DomainEnrolmentRecordCreation
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordDeletion as DomainEnrolmentRecordDeletion
import com.simprints.id.data.db.person.domain.personevents.EnrolmentRecordMove as DomainEnrolmentRecordMove

@Keep
sealed class ApiEnrolmentRecordOperation(val type: ApiEnrolmentRecordOperationType)

@Keep
class ApiEnrolmentRecordCreation(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String,
                                 val biometricReferences: List<ApiBiometricReference>) : ApiEnrolmentRecordOperation(EnrolmentRecordCreation)

@Keep
class ApiEnrolmentRecordDeletion(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String) : ApiEnrolmentRecordOperation(EnrolmentRecordDeletion)

@Keep
class ApiEnrolmentRecordMove(val enrolmentRecordCreation: ApiEnrolmentRecordCreation,
                             val enrolmentRecordDeletion: ApiEnrolmentRecordDeletion) : ApiEnrolmentRecordOperation(EnrolmentRecordMove)

@Keep
enum class ApiEnrolmentRecordOperationType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}

fun EnrolmentRecordOperation.fromDomainToApi() = when (this) {
    is DomainEnrolmentRecordCreation -> this.fromDomainToApi()
    is DomainEnrolmentRecordDeletion -> this.fromDomainToApi()
    is DomainEnrolmentRecordMove -> this.fromDomainToApi()
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
