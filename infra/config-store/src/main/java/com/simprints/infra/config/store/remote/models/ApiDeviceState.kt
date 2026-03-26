package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.DeviceState
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiDeviceState(
    val deviceId: String,
    val isCompromised: Boolean,
    val mustUpSyncEnrolmentRecords: ApiUpSyncEnrolmentRecords? = null,
    val mustUpdateDeviceConfiguration: ApiMustUpdateDeviceConfiguration? = null,
) {
    fun toDomain() = DeviceState(
        deviceId = deviceId,
        isCompromised = isCompromised,
        recordsToUpSync = mustUpSyncEnrolmentRecords?.fromApiToDomain(),
        selectModules = mustUpdateDeviceConfiguration?.fromApiToDomain(),
    )
}
