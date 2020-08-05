package com.simprints.id.services.sync.subjects.up.controllers

import androidx.work.OneTimeWorkRequest

interface SubjectsUpSyncWorkersBuilder {

    fun buildUpSyncWorkerChain(uniqueSyncId: String?): List<OneTimeWorkRequest>
}
