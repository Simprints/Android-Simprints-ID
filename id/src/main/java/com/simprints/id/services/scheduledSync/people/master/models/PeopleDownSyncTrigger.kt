package com.simprints.id.services.scheduledSync.people.master

import androidx.annotation.Keep

@Keep
enum class PeopleDownSyncTrigger {
    MANUAL,
    PERIODIC_BACKGROUND,
    ON_LAUNCH_CALLOUT
}
