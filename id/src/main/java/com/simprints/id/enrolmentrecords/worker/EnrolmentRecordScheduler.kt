package com.simprints.id.enrolmentrecords.worker

interface EnrolmentRecordScheduler {
    fun upload(id: String, subjectIds: List<String>)
}

