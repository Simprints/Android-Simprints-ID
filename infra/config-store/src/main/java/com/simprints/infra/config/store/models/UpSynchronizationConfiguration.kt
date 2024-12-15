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
        val upSyncs: Int,
        val downSyncs: Int,
    ) {
        companion object {
            fun default() = UpSyncBatchSizes(1, 1, 1)
        }
    }

    enum class UpSynchronizationKind {
        NONE,
        ONLY_ANALYTICS,
        ONLY_BIOMETRICS,
        ALL,
    }
}
