package com.simprints.id.services.sync.subjects.down.controllers

import androidx.work.OneTimeWorkRequest

interface SubjectsDownSyncWorkersBuilder {

    suspend fun buildDownSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>

}
