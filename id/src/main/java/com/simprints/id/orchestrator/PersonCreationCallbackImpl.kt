package com.simprints.id.orchestrator

import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.analytics.eventdata.models.domain.events.EnrolmentEvent
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.orchestrator.responsebuilders.AppResponseBuilderForEnrol
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonCreationCallbackImpl(
    private val repository: PersonRepository,
    private val sessionEventsManager: SessionEventsManager,
    private val timeHelper: TimeHelper
) : AppResponseBuilderForEnrol.PersonCreationCallback {

    override fun onPersonCreated(person: Person?) {
        person?.let {
            saveAndUpload(it)
            registerEvent(it)
        }
    }

    private fun saveAndUpload(person: Person) {
        CoroutineScope(Dispatchers.Default).launch {
            repository.saveAndUpload(person)
        }
    }

    private fun registerEvent(person: Person) {
        sessionEventsManager
            .updateSession {
                it.addEvent(
                    EnrolmentEvent(timeHelper.now(), person.patientId)
                )
            }
    }

}
