package com.simprints.id.services.scheduledSync.people.down.controllers

import androidx.work.OneTimeWorkRequest

interface PeopleDownSyncWorkersBuilder {

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>

}
