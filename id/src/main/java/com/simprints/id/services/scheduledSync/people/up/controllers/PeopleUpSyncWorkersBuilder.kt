package com.simprints.id.services.scheduledSync.people.up.controllers

import androidx.work.OneTimeWorkRequest

interface PeopleUpSyncWorkersBuilder {

    fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>
}
