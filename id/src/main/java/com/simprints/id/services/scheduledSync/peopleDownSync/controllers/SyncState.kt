package com.simprints.id.services.scheduledSync.peopleDownSync.controllers

import androidx.annotation.Keep

@Keep
class SyncState(val syncId: String,
                val state: State) {

    constructor(syncId: String, anyRunning: Boolean, anyEnqueued: Boolean, anyNotSucceeded: Boolean) : this(
        syncId = syncId,
        state = State.build(anyRunning, anyEnqueued, anyNotSucceeded)
    )

    enum class State {
        ENQUEUED,
        RUNNING,
        COMPLETED,
        FAILED;

        companion object {
            fun build(anyRunning: Boolean, anyEnqueued: Boolean, anyNotSucceeded: Boolean) =
                when {
                    anyRunning -> {
                        RUNNING
                    }
                    anyEnqueued -> {
                        ENQUEUED
                    }
                    anyNotSucceeded -> {
                        FAILED
                    }
                    else -> {
                        COMPLETED
                    }
                }
        }
    }
}
