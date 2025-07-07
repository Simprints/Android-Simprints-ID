package com.simprints.infra.config.store.models

data class UpSynchronizationConfiguration(
    val simprints: SimprintsUpSynchronizationConfiguration,
    val coSync: CoSyncUpSynchronizationConfiguration,
) {
    data class SimprintsUpSynchronizationConfiguration(
        val kind: UpSynchronizationKind,
        val batchSizes: UpSyncBatchSizes,
        val imagesRequireUnmeteredConnection: Boolean,
    )

    data class CoSyncUpSynchronizationConfiguration(
        val kind: UpSynchronizationKind,
    )

    data class UpSyncBatchSizes(
        val sessions: Int,
        val eventUpSyncs: Int,
        val eventDownSyncs: Int,
        val sampleUpSyncs: Int,
    ) {
        companion object {
            fun default() = UpSyncBatchSizes(sessions = 1, eventUpSyncs = 1, eventDownSyncs = 1, sampleUpSyncs = 1)
        }
    }

    enum class UpSynchronizationKind {
        NONE,
        ONLY_ANALYTICS,
        ONLY_BIOMETRICS,
        ALL,
    }
}
