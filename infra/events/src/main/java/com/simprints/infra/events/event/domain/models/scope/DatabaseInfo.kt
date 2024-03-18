package com.simprints.infra.events.event.domain.models.scope

import androidx.annotation.Keep

@Keep
data class DatabaseInfo(
    val sessionCount: Int,
    var recordCount: Int? = null,
)
