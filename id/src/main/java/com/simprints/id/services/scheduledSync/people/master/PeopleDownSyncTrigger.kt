package com.simprints.id.services.scheduledSync.people.down.models

import androidx.annotation.Keep

@Keep
enum class PeopleDownSyncTrigger {
    MANUAL,
    PERIODIC_BACKGROUND,
    ON_LAUNCH_CALLOUT
}
