package com.simprints.infra.enrolment.records.repository.remote

import com.simprints.infra.enrolment.records.repository.domain.models.Subject

fun interface EnrolmentRecordRemoteDataSource {
    suspend fun uploadRecords(subjects: List<Subject>)
}
