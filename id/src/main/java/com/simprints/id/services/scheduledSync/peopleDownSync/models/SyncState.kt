package com.simprints.id.services.scheduledSync.peopleDownSync.models

import androidx.annotation.Keep

@Keep
enum class SyncState {
    NOT_RUNNING,
    RUNNING,
    CALCULATING
}
