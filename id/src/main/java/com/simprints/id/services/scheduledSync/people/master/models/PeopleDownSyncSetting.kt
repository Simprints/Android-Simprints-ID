package com.simprints.id.services.scheduledSync.people.master.models

import io.realm.internal.Keep

@Keep
enum class PeopleDownSyncSetting {
    ON,
    OFF,
    EXTRA
}
