package com.simprints.id.enrolmentrecords.remote

import com.simprints.id.data.db.subject.domain.Subject

interface EnrolmentRecordRemoteDataSource {

    suspend fun uploadRecords(subjects: List<Subject>)

}
