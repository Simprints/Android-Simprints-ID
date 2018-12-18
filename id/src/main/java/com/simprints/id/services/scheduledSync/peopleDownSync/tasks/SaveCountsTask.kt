package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope

interface SaveCountsTask {

    fun execute(countsForSubScopes: Map<SubSyncScope, Int>)

}
