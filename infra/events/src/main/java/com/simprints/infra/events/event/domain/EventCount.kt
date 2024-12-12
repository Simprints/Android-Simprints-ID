package com.simprints.infra.events.event.domain

import androidx.annotation.Keep

@Keep
data class EventCount(
    val count: Int,
    val isLowerBound: Boolean,
) {
    /**
     * Returns exact count or null if the count is a lower bound.
     */
    val exactCount: Int?
        get() = if (isLowerBound) null else count
}
