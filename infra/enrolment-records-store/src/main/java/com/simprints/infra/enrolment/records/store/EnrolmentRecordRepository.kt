package com.simprints.infra.enrolment.records.store

interface EnrolmentRecordRepository {
    suspend fun uploadRecords(subjectIds: List<String>)
}
