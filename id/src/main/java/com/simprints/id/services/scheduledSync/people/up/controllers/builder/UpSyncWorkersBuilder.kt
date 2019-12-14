package com.simprints.id.services.scheduledSync.people.up.controllers.builder

import androidx.work.WorkRequest

interface UpSyncWorkerBuilder {

    fun buildUpSyncWorkerChain(): List<WorkRequest>
}
