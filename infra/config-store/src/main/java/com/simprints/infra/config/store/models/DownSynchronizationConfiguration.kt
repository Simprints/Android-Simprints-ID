package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.Partitioning
import com.simprints.core.domain.tokenization.TokenizableString

data class DownSynchronizationConfiguration(
    val partitionType: PartitionType,
    val maxNbOfModules: Int,
    val moduleOptions: List<TokenizableString>
) {

    enum class PartitionType {
        PROJECT,
        MODULE,
        USER;

        fun toDomain(): Partitioning = when (this) {
            PROJECT -> Partitioning.GLOBAL
            MODULE -> Partitioning.MODULE
            USER -> Partitioning.USER
        }
    }
}
