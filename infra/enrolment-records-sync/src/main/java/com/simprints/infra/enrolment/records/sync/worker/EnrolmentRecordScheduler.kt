package com.simprints.infra.enrolment.records.sync.worker

interface EnrolmentRecordScheduler {
    fun upload(id: String, subjectIds: List<String>)
}

