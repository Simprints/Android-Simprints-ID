package com.simprints.core.tools.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TimerImpl @Inject constructor() : Timer {
    override fun observeTickOncePerMinute(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(ONE_MINUTE_IN_MILLIS)
        }
    }

    private companion object {
        private const val ONE_MINUTE_IN_MILLIS = 60 * 1000L
    }
}
