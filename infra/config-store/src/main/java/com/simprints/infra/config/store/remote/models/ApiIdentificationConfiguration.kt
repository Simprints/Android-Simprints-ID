package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.IdentificationConfiguration

@Keep
internal data class ApiIdentificationConfiguration(
    val maxNbOfReturnedCandidates: Int,
    val poolType: PoolType,
) {
    fun toDomain(): IdentificationConfiguration = IdentificationConfiguration(
        maxNbOfReturnedCandidates,
        poolType.toDomain(),
    )

    @Keep
    enum class PoolType {
        PROJECT,
        MODULE,
        USER,
        ;

        fun toDomain(): IdentificationConfiguration.PoolType = when (this) {
            PROJECT -> IdentificationConfiguration.PoolType.PROJECT
            MODULE -> IdentificationConfiguration.PoolType.MODULE
            USER -> IdentificationConfiguration.PoolType.USER
        }
    }
}
