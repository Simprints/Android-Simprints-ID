package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.DeviceState

@Keep
internal data class ApiDeviceState(
    val deviceId: String,
    val isCompromised: Boolean,
    val mustUpSyncEnrolmentRecords: ApiUpSyncEnrolmentRecords? = null,
) {
    fun toDomain() = DeviceState(
        deviceId,
        isCompromised,
        mustUpSyncEnrolmentRecords?.fromApiToDomain(),
    )
}
