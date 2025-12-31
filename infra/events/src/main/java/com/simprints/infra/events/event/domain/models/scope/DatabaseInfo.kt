package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class DatabaseInfo(
    val sessionCount: Int,
    var recordCount: Int? = null,
)
