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
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Down-syncable event counting relies on non-reactive (by design) call to EventDownSyncCountsRepository.
 * To wrap it in a quasi-reactive way, that call is triggered
 * on instantiation (for a "pre-warmed" count), and
 * every 5 minutes while there are subscribers to invoke().
 */
@Singleton
class EventDownSyncPeriodicCountUseCase @Inject constructor(
    private val eventDownSyncCountsRepository: EventDownSyncCountsRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedDownSyncCounts: SharedFlow<DownSyncCounts> = MutableSharedFlow<DownSyncCounts>(replay = 1)
        .apply {
            appScope.launch {
                tryCountEventsAndEmit() // "pre-warmed" count for immediate display as an initial estimate
                subscriptionCount.map { it > 0 }.distinctUntilChanged().collectLatest { hasSubscribers ->
                    while (hasSubscribers && isActive) {
                        tryCountEventsAndEmit()
                        delay(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
                    }
                }
            }
        }.asSharedFlow()

    operator fun invoke(): Flow<DownSyncCounts> = sharedDownSyncCounts

    private suspend fun MutableSharedFlow<DownSyncCounts>.tryCountEventsAndEmit() {
        val fallbackDefaultCounts = DownSyncCounts(count = 0, isLowerBound = false)
        val counts = try {
            withTimeoutOrNull(DOWN_SYNC_COUNT_TIMEOUT_MILLIS) {
                eventDownSyncCountsRepository.countEventsToDownload()
            } ?: fallbackDefaultCounts
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (t: Throwable) {
            Simber.i("Events to download counting error", t, tag = SYNC)
            fallbackDefaultCounts
        }
        emit(counts)
    }

    companion object {
        private const val DOWN_SYNC_COUNT_TIMEOUT_MILLIS = 10_000L
        internal const val DOWN_SYNC_COUNT_INTERVAL_MILLIS = 300_000L
    }
}
