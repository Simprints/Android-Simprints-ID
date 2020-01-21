package com.simprints.id.services.scheduledSync.people.master.models

import androidx.work.WorkInfo

sealed class PeopleWorkerState {
    object Enqueued : PeopleWorkerState()
    object Running : PeopleWorkerState()
    object Succeeded : PeopleWorkerState()
    class Failed(val failedBecauseCloudIntegration: Boolean) : PeopleWorkerState()
    object Blocked : PeopleWorkerState()
    object Cancelled : PeopleWorkerState()

    companion object {
        fun fromWorkInfo(state: WorkInfo.State,
                         failedBecauseCloudIntegration: Boolean = false) =
            when (state) {
                WorkInfo.State.ENQUEUED -> Enqueued
                WorkInfo.State.RUNNING -> Running
                WorkInfo.State.SUCCEEDED -> Succeeded
                WorkInfo.State.FAILED -> Failed(failedBecauseCloudIntegration)
                WorkInfo.State.BLOCKED -> Blocked
                WorkInfo.State.CANCELLED -> Cancelled
            }
    }
}
