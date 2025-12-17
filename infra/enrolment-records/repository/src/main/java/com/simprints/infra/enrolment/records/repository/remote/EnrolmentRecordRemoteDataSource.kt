package com.simprints.infra.enrolment.records.repository.remote

import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecord

fun interface EnrolmentRecordRemoteDataSource {
    suspend fun uploadRecords(enrolmentRecords: List<EnrolmentRecord>)
}
