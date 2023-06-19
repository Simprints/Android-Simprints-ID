package com.simprints.infra.enrolment.records.remote

import com.simprints.infra.enrolment.records.domain.models.Subject

interface EnrolmentRecordRemoteDataSource {

    suspend fun uploadRecords(subjects: List<Subject>)

}
