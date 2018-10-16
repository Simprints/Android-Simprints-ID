package com.simprints.id.services.scheduledSync.peopleUpsync

import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderMaster
import timber.log.Timber

class PeopleUpSyncMaster(
    private val peopleUpSyncUploaderMaster: PeopleUpSyncUploaderMaster,
    private val peopleUpSyncPeriodicFlusherMaster: PeopleUpSyncPeriodicFlusherMaster
) {

    fun schedule(projectId: String, userId: String) {
        Timber.d("Scheduling upsync")
        peopleUpSyncUploaderMaster.schedule(projectId, userId)
    }

    fun pause(projectId: String, userId: String) {
        peopleUpSyncPeriodicFlusherMaster.disablePeriodicFlusherFor(projectId, userId)
        peopleUpSyncUploaderMaster.cancel(projectId, userId)
    }

    fun resume(projectId: String, userId: String) {
        peopleUpSyncUploaderMaster.schedule(projectId, userId)
        peopleUpSyncPeriodicFlusherMaster.enablePeriodicFlusherFor(projectId, userId)
    }
}
