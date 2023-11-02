package com.simprints.infra.enrolment.records.store

import com.simprints.infra.config.store.models.Project

interface EnrolmentRecordRepository {
    suspend fun uploadRecords(subjectIds: List<String>)
    suspend fun tokenizeExistingRecords(project: Project)
}
