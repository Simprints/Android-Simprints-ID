package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Single

interface CountTask {

    fun execute(subSyncScope: SubSyncScope): Single<Int>
}
