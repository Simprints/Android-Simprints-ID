package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.Partitioning
import com.simprints.core.domain.tokenization.TokenizableString

data class DownSynchronizationConfiguration(
    val partitionType: PartitionType,
    val maxNbOfModules: Int,
    val moduleOptions: List<TokenizableString>,
    val maxAge: String,
) {
    enum class PartitionType {
        PROJECT,
        MODULE,
        USER,
        ;

        fun toDomain(): Partitioning = when (this) {
            PROJECT -> Partitioning.GLOBAL
            MODULE -> Partitioning.MODULE
            USER -> Partitioning.USER
        }
    }

    companion object {
        const val DEFAULT_DOWN_SYNC_MAX_AGE = "PT24H"
    }
}
