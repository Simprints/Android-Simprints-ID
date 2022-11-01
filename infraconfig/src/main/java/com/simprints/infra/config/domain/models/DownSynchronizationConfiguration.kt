package com.simprints.infra.config.domain.models

data class DownSynchronizationConfiguration(
    val partitionType: PartitionType,
    val maxNbOfModules: Int,
    val moduleOptions: List<String>?
) {

    enum class PartitionType {
        PROJECT,
        MODULE,
        USER;
    }
}
