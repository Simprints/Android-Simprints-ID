package com.simprints.infra.config.store.models

data class SynchronizationConfiguration(
    val frequency: Frequency,
    val up: UpSynchronizationConfiguration,
    val down: DownSynchronizationConfiguration,
) {
    enum class Frequency {
        ONLY_PERIODICALLY_UP_SYNC,
        PERIODICALLY,
        PERIODICALLY_AND_ON_SESSION_START,
    }
}
