package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.DeviceUsage

@Keep
internal data class ApiDeviceUsage(
    val availableStorageMb: Long?,
    val availableRamMb: Long?,
    val isBatterySaverOn: Boolean?,
)

internal fun DeviceUsage.fromDomainToApi() = ApiDeviceUsage(
    availableStorageMb = availableStorageMb,
    availableRamMb = availableRamMb,
    isBatterySaverOn = isBatterySaverOn,
)
