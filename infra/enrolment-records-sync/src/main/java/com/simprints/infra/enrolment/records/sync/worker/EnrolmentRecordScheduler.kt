package com.simprints.infra.enrolment.records.worker

interface EnrolmentRecordScheduler {
    fun upload(id: String, subjectIds: List<String>)
}

