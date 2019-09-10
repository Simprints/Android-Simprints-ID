package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope

class CountTaskImpl(private val personRepository: PersonRepository) : CountTask {

    override fun execute(syncScope: SyncScope) =
        personRepository.countToDownSync(syncScope)
}
