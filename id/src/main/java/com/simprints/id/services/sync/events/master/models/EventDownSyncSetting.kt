package com.simprints.id.services.sync.events.master.models

import io.realm.internal.Keep

@Keep
enum class EventDownSyncSetting {
    ON,
    OFF,
    EXTRA
}
