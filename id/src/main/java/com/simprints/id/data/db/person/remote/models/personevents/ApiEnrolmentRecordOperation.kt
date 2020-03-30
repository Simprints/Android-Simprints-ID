package com.simprints.id.data.db.person.remote.models.personevents

import com.simprints.id.data.db.person.remote.models.personevents.ApiEnrolmentRecordOperationType.*

sealed class ApiEnrolmentRecordOperation(val type: ApiEnrolmentRecordOperationType)

class ApiEnrolmentRecordCreation(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String,
                                 val biometricReferences: Array<ApiBiometricReference>): ApiEnrolmentRecordOperation(EnrolmentRecordCreation)

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
