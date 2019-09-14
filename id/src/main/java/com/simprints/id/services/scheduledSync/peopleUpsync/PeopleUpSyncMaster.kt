package com.simprints.id.services.scheduledSync.peopleUpsync

interface PeopleUpSyncMaster {
    fun schedule(projectId: String)
    fun pause(projectId: String)
    fun resume(projectId: String)
}
