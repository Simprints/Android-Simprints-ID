package com.simprints.core.tools.time

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

@Keep
interface Ticker {
    fun observeTickOncePerMinute(): Flow<Unit>
}
