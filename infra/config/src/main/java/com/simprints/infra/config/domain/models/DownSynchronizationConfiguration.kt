package com.simprints.infra.config.domain.models

import com.simprints.core.domain.common.GROUP

data class DownSynchronizationConfiguration(
    val partitionType: PartitionType,
    val maxNbOfModules: Int,
    val moduleOptions: List<String>
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
