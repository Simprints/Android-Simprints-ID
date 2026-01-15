package com.simprints.infra.eventsync.sync.down

import com.simprints.core.AppScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.eventsync.status.models.DownSyncCounts
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Down-syncable event counting relies on non-reactive (by design) call to EventDownSyncCountsRepository.
 * To wrap it in a quasi-reactive way, that call is triggered
 * every 5 minutes while there are subscribers to invoke(), and no less than 10 seconds apart.
 */
@Singleton
class EventDownSyncPeriodicCountUseCase @Inject constructor(
    private val eventDownSyncCountsRepository: EventDownSyncCountsRepository,
    private val timeHelper: TimeHelper,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private var lastCountTimestamp: Long = 0
    private val sharedDownSyncCounts: SharedFlow<DownSyncCounts> = MutableSharedFlow<DownSyncCounts>(replay = 1).apply {
        appScope.launch {
            tryCountEventsAndEmit() // initial count
            subscriptionCount.map { it > 0}.distinctUntilChanged().collectLatest { hasSubscribers ->
                val isEligibleToPeriodicCounting =
                    hasSubscribers && isDebounceTimeOut() && isActive
                while (isEligibleToPeriodicCounting) {
                    tryCountEventsAndEmit()
                    delay(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
                }
            }
        }
    }.asSharedFlow()

    operator fun invoke(): Flow<DownSyncCounts> = sharedDownSyncCounts

    private suspend fun MutableSharedFlow<DownSyncCounts>.tryCountEventsAndEmit() {
        lastCountTimestamp = timeHelper.now().ms
        try {
            withTimeout(DOWN_SYNC_COUNT_DEBOUNCE_MILLIS) {
                emit(eventDownSyncCountsRepository.countEventsToDownload())
            }
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (t: Throwable) {
            Simber.i("Events to download counting error", t, tag = SYNC)
        }
    }

    private fun isDebounceTimeOut(): Boolean =
        timeHelper.now().ms - lastCountTimestamp > DOWN_SYNC_COUNT_DEBOUNCE_MILLIS

    companion object {
        internal const val DOWN_SYNC_COUNT_DEBOUNCE_MILLIS = 10_000L
        internal const val DOWN_SYNC_COUNT_INTERVAL_MILLIS = 300_000L
    }
}
