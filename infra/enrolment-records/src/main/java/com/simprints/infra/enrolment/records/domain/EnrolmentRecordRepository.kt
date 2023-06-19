package com.simprints.infra.enrolment.records.domain

internal interface EnrolmentRecordRepository {
    suspend fun uploadRecords(subjectIds: List<String>)
}
