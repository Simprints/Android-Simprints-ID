package com.simprints.id.orchestrator

import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.EnrolmentEvent
import com.simprints.id.tools.TimeHelper

class EnrolmentHelperImpl(private val repository: SubjectRepository,
                          private val sessionRepository: SessionRepository,
                          private val timeHelper: TimeHelper) : EnrolmentHelper {

    override suspend fun saveAndUpload(subject: Subject) {
        repository.saveAndUpload(subject)
    }

    override fun registerEvent(subject: Subject) {
        with(sessionRepository) {
            addEventToCurrentSessionInBackground(EnrolmentEvent(
                timeHelper.now(),
                subject.subjectId
            ))
        }
    }

}
