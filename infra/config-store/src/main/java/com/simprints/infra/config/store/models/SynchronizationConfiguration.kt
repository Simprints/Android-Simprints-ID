package com.simprints.infra.config.store.models

data class SynchronizationConfiguration(
    val up: UpSynchronizationConfiguration,
    val down: DownSynchronizationConfiguration,
    val samples: SampleSynchronizationConfiguration,
)
