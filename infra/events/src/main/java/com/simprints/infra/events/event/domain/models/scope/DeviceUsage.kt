package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep

@Keep
data class DeviceUsage(
    val availableStorageMb: Long?,
    val availableRamMb: Long?,
    val isBatterySaverOn: Boolean?,
)
