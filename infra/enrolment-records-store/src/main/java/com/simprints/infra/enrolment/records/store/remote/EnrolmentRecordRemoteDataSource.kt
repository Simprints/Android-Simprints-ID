package com.simprints.infra.enrolment.records.store.remote

import com.simprints.infra.enrolment.records.store.domain.models.Subject

interface EnrolmentRecordRemoteDataSource {
    suspend fun uploadRecords(subjects: List<Subject>)
}
