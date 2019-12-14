package com.simprints.id.services.scheduledSync.people.down.models

import androidx.annotation.Keep

@Keep
class SyncState(val syncId: String,
                val progress: Int?,
                val total: Int,
                val state: State) {

    enum class State {
        ENQUEUED,
        RUNNING,
        COMPLETED,
        FAILED;
    }
}
