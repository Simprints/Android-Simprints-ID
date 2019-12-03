package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.data.db.person.domain.PeopleCount
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope

interface CountTask {

    suspend fun execute(syncScope: SyncScope): List<PeopleCount>
}
