package com.simprints.id.domain

import androidx.annotation.Keep

@Keep
enum class SyncDestinationSetting {
    SIMPRINTS,
    COMMCARE
}

fun List<SyncDestinationSetting>.containsCommcare() =
    this.contains(SyncDestinationSetting.COMMCARE)

fun List<SyncDestinationSetting>.containsSimprints() =
    this.contains(SyncDestinationSetting.SIMPRINTS)
