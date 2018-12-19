package com.simprints.id.services.scheduledSync.peopleDownSync.tasks

import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Completable

interface DownSyncTask {

    fun execute(subSyncScope: SubSyncScope): Completable
}
