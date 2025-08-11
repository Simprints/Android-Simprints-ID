package com.simprints.core.tools.time

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

@Keep
interface Timer {
    fun observeTickOncePerMinute(): Flow<Unit>
}
