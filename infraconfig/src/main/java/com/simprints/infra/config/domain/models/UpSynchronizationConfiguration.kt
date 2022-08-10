package com.simprints.infra.config.domain.models

data class UpSynchronizationConfiguration(
    val simprints: SimprintsUpSynchronizationConfiguration,
    val coSync: CoSyncUpSynchronizationConfiguration,
) {

    data class SimprintsUpSynchronizationConfiguration(val kind: UpSynchronizationKind)

    data class CoSyncUpSynchronizationConfiguration(val kind: UpSynchronizationKind)

    enum class UpSynchronizationKind {
        NONE,
        ONLY_ANALYTICS,
        ONLY_BIOMETRICS,
        ALL;
    }
}
