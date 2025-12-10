package com.simprints.core.tools.time

import androidx.annotation.Keep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

@Keep
@Singleton
class Ticker @Inject constructor() {
    fun observeTicks(interval: Duration): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(interval)
        }
    }
}
