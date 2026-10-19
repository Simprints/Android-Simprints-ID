package com.simprints.infra.events.event.local.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.scope.EventScopeType

@Keep
internal data class DbScopeTypeCount(
    val type: EventScopeType,
    val count: Int,
)
