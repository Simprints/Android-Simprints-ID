package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.DownSynchronizationConfiguration
import com.simprints.infra.config.domain.models.SynchronizationConfiguration
import com.simprints.infra.config.domain.models.UpSynchronizationConfiguration

@Keep
internal data class ApiSynchronizationConfiguration(
    val frequency: Frequency,
    val up: ApiUpSynchronizationConfiguration,
    val down: ApiDownSynchronizationConfiguration,
) {

    fun toDomain(): SynchronizationConfiguration =
        SynchronizationConfiguration(
            frequency.toDomain(),
            up.toDomain(),
            down.toDomain()
        )

    @Keep
    enum class Frequency {
        ONLY_PERIODICALLY_UP_SYNC,
        PERIODICALLY,
        PERIODICALLY_AND_ON_SESSION_START;

        fun toDomain(): SynchronizationConfiguration.Frequency =
            when (this) {
                ONLY_PERIODICALLY_UP_SYNC -> SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
                PERIODICALLY -> SynchronizationConfiguration.Frequency.PERIODICALLY
                PERIODICALLY_AND_ON_SESSION_START -> SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
            }
    }

    @Keep
    data class ApiUpSynchronizationConfiguration(
        val simprints: ApiSimprintsUpSynchronizationConfiguration,
        val coSync: ApiCoSyncUpSynchronizationConfiguration,
    ) {

        fun toDomain(): UpSynchronizationConfiguration =
            UpSynchronizationConfiguration(simprints.toDomain(), coSync.toDomain())

        @Keep
        data class ApiSimprintsUpSynchronizationConfiguration(val kind: UpSynchronizationKind) {
            fun toDomain(): UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration =
                UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(kind.toDomain())
        }

        @Keep
        data class ApiCoSyncUpSynchronizationConfiguration(val kind: UpSynchronizationKind) {
            fun toDomain(): UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration =
                UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(kind.toDomain())
        }

        @Keep
        enum class UpSynchronizationKind {
            NONE,
            ONLY_ANALYTICS,
            ONLY_BIOMETRICS,
            ALL;

            fun toDomain(): UpSynchronizationConfiguration.UpSynchronizationKind =
                when (this) {
                    NONE -> UpSynchronizationConfiguration.UpSynchronizationKind.NONE
                    ONLY_ANALYTICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
                    ONLY_BIOMETRICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
                    ALL -> UpSynchronizationConfiguration.UpSynchronizationKind.ALL
                }
        }
    }

    @Keep
    data class ApiDownSynchronizationConfiguration(
        val partitionType: PartitionType,
        val maxNbOfModules: Int,
        val moduleOptions: List<String>?
    ) {

        fun toDomain(): DownSynchronizationConfiguration =
            DownSynchronizationConfiguration(
                partitionType.toDomain(),
                maxNbOfModules,
                moduleOptions
            )

        @Keep
        enum class PartitionType {
            PROJECT,
            MODULE,
            USER;

            fun toDomain(): DownSynchronizationConfiguration.PartitionType =
                when (this) {
                    PROJECT -> DownSynchronizationConfiguration.PartitionType.PROJECT
                    MODULE -> DownSynchronizationConfiguration.PartitionType.MODULE
                    USER -> DownSynchronizationConfiguration.PartitionType.USER
                }
        }
    }
}
