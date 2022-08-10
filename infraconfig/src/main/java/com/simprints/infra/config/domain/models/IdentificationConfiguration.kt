package com.simprints.infra.config.domain.models

data class IdentificationConfiguration(
    val maxNbOfReturnedCandidates: Int,
    val poolType: PoolType,
) {

    enum class PoolType {
        PROJECT,
        MODULE,
        USER;
    }
}
