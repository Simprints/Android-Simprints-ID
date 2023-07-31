package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.Vero1Configuration

@Keep
internal data class ApiVero1Configuration(
    val qualityThreshold: Int,
) {

    fun toDomain(): Vero1Configuration =
        Vero1Configuration(
            qualityThreshold
        )
}
