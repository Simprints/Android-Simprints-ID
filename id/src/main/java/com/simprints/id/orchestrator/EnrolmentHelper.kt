package com.simprints.id.orchestrator

import com.simprints.id.data.db.subject.domain.Subject

interface EnrolmentHelper {

    suspend fun saveAndUpload(subject: Subject)
    fun registerEvent(subject: Subject)

}
