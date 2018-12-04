package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Completable
import io.reactivex.Single


interface SaveCountsTask {

    fun execute(countsForSubScopes: Map<SubSyncScope, Int>)

}
