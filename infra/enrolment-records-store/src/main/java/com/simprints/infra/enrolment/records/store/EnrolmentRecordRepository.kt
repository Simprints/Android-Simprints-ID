package com.simprints.infra.enrolment.records.store

import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.store.local.EnrolmentRecordLocalDataSource

interface EnrolmentRecordRepository : EnrolmentRecordLocalDataSource {
    suspend fun uploadRecords(subjectIds: List<String>)
    suspend fun tokenizeExistingRecords(project: Project)
}
