package com.simprints.infra.config.store.models

data class DeviceState(
    val deviceId: String,
    val isCompromised: Boolean,
    val recordsToUpSync: UpSyncEnrolmentRecords? = null,
)
