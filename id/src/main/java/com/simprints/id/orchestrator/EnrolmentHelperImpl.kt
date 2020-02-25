package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.session.domain.SessionEventsManager
import com.simprints.id.data.db.session.domain.models.events.EnrolmentEvent
import com.simprints.id.tools.TimeHelper

class EnrolmentHelperImpl(private val repository: PersonRepository,
                          private val sessionEventsManager: SessionEventsManager,
                          private val timeHelper: TimeHelper) : EnrolmentHelper {

    override suspend fun saveAndUpload(person: Person) {
        repository.saveAndUpload(person)
    }

    override fun registerEvent(person: Person) {
        with(sessionEventsManager) {
            addEventInBackground(EnrolmentEvent(
                timeHelper.now(),
                person.patientId
            ))
        }
    }

}
