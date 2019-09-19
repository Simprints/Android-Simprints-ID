package com.simprints.id.services.scheduledSync.peopleUpsync

import com.simprints.id.services.scheduledSync.peopleUpsync.periodicFlusher.PeopleUpSyncPeriodicFlusherMaster
import com.simprints.id.services.scheduledSync.peopleUpsync.uploader.PeopleUpSyncUploaderMaster
import timber.log.Timber

// TODO: uncomment userId when multitenancy is properly implemented
open class PeopleUpSyncMasterImpl(
    private val peopleUpSyncUploaderMaster: PeopleUpSyncUploaderMaster,
    private val peopleUpSyncPeriodicFlusherMaster: PeopleUpSyncPeriodicFlusherMaster
) : PeopleUpSyncMaster {

    override fun schedule(projectId: String/*, userId: String*/) {
        Timber.d("Scheduling upsync")
        peopleUpSyncUploaderMaster.schedule(projectId/*, userId*/)
    }

    override fun pause(projectId: String/*, userId: String*/) {
        peopleUpSyncPeriodicFlusherMaster.disablePeriodicFlusherFor(projectId/*, userId*/)
        peopleUpSyncUploaderMaster.cancel(projectId/*, userId*/)
    }

    override fun resume(projectId: String/*, userId: String*/) {
        peopleUpSyncUploaderMaster.schedule(projectId/*, userId*/)
        peopleUpSyncPeriodicFlusherMaster.enablePeriodicFlusherFor(projectId/*, userId*/)
    }
}
