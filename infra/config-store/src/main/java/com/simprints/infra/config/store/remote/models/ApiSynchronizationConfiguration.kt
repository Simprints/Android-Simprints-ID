package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration.Companion.DEFAULT_DOWN_SYNC_MAX_AGE
import com.simprints.infra.config.store.models.Frequency
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.SynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration

@Keep
internal data class ApiSynchronizationConfiguration(
    val up: ApiUpSynchronizationConfiguration,
    val down: ApiDownSynchronizationConfiguration,
    val sample: ApiSampleSynchronizationConfiguration,
) {
    fun toDomain(): SynchronizationConfiguration = SynchronizationConfiguration(
        up = up.toDomain(),
        down = down.toDomain(),
        samples = sample.toDomain(),
    )

    @Keep
    enum class ApiSynchronizationFrequency {
        ONLY_PERIODICALLY_UP_SYNC,
        PERIODICALLY,
        PERIODICALLY_AND_ON_SESSION_START,
        ;

        fun toDomain(): Frequency = when (this) {
            ONLY_PERIODICALLY_UP_SYNC -> Frequency.ONLY_PERIODICALLY_UP_SYNC
            PERIODICALLY -> Frequency.PERIODICALLY
            PERIODICALLY_AND_ON_SESSION_START -> Frequency.PERIODICALLY_AND_ON_SESSION_START
        }
    }

    @Keep
    data class ApiUpSynchronizationConfiguration(
        val simprints: ApiSimprintsUpSynchronizationConfiguration,
        val coSync: ApiCoSyncUpSynchronizationConfiguration,
    ) {
        fun toDomain(): UpSynchronizationConfiguration = UpSynchronizationConfiguration(
            simprints.toDomain(),
            coSync.toDomain(),
        )

        @Keep
        data class ApiSimprintsUpSynchronizationConfiguration(
            val kind: UpSynchronizationKind,
            val batchSizes: ApiUpSyncBatchSizes?,
            val imagesRequireUnmeteredConnection: Boolean?,
            val frequency: ApiSynchronizationFrequency,
        ) {
            fun toDomain(): UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration =
                UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
                    kind = kind.toDomain(),
                    batchSizes = batchSizes?.toDomain()
                        ?: UpSynchronizationConfiguration.UpSyncBatchSizes.default(),
                    imagesRequireUnmeteredConnection = imagesRequireUnmeteredConnection ?: false,
                    frequency = frequency.toDomain(),
                )
        }

        @Keep
        data class ApiCoSyncUpSynchronizationConfiguration(
            val kind: UpSynchronizationKind,
        ) {
            fun toDomain(): UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration =
                UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(kind.toDomain())
        }

        @Keep
        enum class UpSynchronizationKind {
            NONE,
            ONLY_ANALYTICS,
            ONLY_BIOMETRICS,
            ALL,
            ;

            fun toDomain(): UpSynchronizationConfiguration.UpSynchronizationKind = when (this) {
                NONE -> UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                ONLY_ANALYTICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
                ONLY_BIOMETRICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
                ALL -> UpSynchronizationConfiguration.UpSynchronizationKind.ALL
            }
        }

        @Keep
        data class ApiUpSyncBatchSizes(
            val sessions: Int,
            val eventUpSyncs: Int,
            val eventDownSyncs: Int,
            val sampleUpSyncs: Int,
        ) {
            fun toDomain(): UpSynchronizationConfiguration.UpSyncBatchSizes =
                UpSynchronizationConfiguration.UpSyncBatchSizes(sessions, eventUpSyncs, eventDownSyncs, sampleUpSyncs)
        }
    }

    @Keep
    data class ApiDownSynchronizationConfiguration(
        val simprints: ApiSimprintsDownSynchronizationConfiguration?,
        val commcare: ApiCommCareDownSynchronizationConfiguration?,
    ) {
        fun toDomain(): DownSynchronizationConfiguration = DownSynchronizationConfiguration(
            simprints?.toDomain(),
            commcare?.toDomain(),
        )
    }

    @Keep
    data class ApiSimprintsDownSynchronizationConfiguration(
        val partitionType: PartitionType,
        val maxNbOfModules: Int,
        val moduleOptions: List<String>?,
        val maxAge: String?,
        val frequency: ApiSynchronizationFrequency,
    ) {
        fun toDomain() = DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration(
            partitionType = partitionType.toDomain(),
            maxNbOfModules = maxNbOfModules,
            moduleOptions = moduleOptions?.map(String::asTokenizableEncrypted) ?: emptyList(),
            maxAge = maxAge ?: DEFAULT_DOWN_SYNC_MAX_AGE,
            frequency = frequency.toDomain(),
        )

        @Keep
        enum class PartitionType {
            PROJECT,
            MODULE,
            USER,
            ;

            fun toDomain(): DownSynchronizationConfiguration.PartitionType = when (this) {
                PROJECT -> DownSynchronizationConfiguration.PartitionType.PROJECT
                MODULE -> DownSynchronizationConfiguration.PartitionType.MODULE
                USER -> DownSynchronizationConfiguration.PartitionType.USER
            }
        }
    }

    @Keep
    class ApiCommCareDownSynchronizationConfiguration {
        fun toDomain() = DownSynchronizationConfiguration.CommCareDownSynchronizationConfiguration
    }

    @Keep
    data class ApiSampleSynchronizationConfiguration(
        val signedUrlBatchSize: Int,
    ) {
        fun toDomain() = SampleSynchronizationConfiguration(
            signedUrlBatchSize = signedUrlBatchSize,
        )
    }
}
