package com.simprints.id.data.db.person.domain.personevents


sealed class EnrolmentRecordOperation(val type: EnrolmentRecordOperationType)

class EnrolmentRecordCreation(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String,
                                 val biometricReferences: List<BiometricReference>): EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordCreation)

class EnrolmentRecordDeletion(val subjectId: String,
                                 val projectId: String,
                                 val moduleId: String,
                                 val attendantId: String): EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordDeletion)

class EnrolmentRecordMove(val enrolmentRecordCreation: EnrolmentRecordCreation,
                             val enrolmentRecordDeletion: EnrolmentRecordDeletion): EnrolmentRecordOperation(EnrolmentRecordOperationType.EnrolmentRecordMove)

enum class EnrolmentRecordOperationType {
    EnrolmentRecordCreation,
    EnrolmentRecordDeletion,
    EnrolmentRecordMove
}
