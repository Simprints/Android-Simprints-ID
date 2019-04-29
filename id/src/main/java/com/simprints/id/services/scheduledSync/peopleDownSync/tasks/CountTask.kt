package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SyncScope
import io.reactivex.Single

interface CountTask {

    fun execute(syncScope: SyncScope): Single<Int>
}
