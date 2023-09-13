package com.simprints.infra.config.domain.models

import com.simprints.core.domain.common.GROUP
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

        fun toGroup(): GROUP = when (this) {
            PROJECT -> GROUP.GLOBAL
            MODULE -> GROUP.MODULE
            USER -> GROUP.USER
        }
    }
}
