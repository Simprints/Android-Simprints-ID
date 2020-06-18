package com.simprints.id.services.scheduledSync.subjects.master.models

import io.realm.internal.Keep

@Keep
enum class SubjectsDownSyncSetting {
    ON,
    OFF,
    EXTRA
}
