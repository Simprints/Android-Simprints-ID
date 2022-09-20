package com.simprints.id.enrolmentrecords

interface EnrolmentRecordRepository {
    suspend fun uploadRecords(subjectIds: List<String>)
}
