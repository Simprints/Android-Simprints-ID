package com.simprints.infra.config.store.models

import com.simprints.core.domain.common.Group
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

        fun toGroup(): Group = when (this) {
            PROJECT -> Group.GLOBAL
            MODULE -> Group.MODULE
            USER -> Group.USER
        }
    }
}
