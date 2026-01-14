package com.simprints.infra.eventsync.sync.down

import com.simprints.core.AppScope
import com.simprints.infra.eventsync.status.models.DownSyncCounts
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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Down-syncable event counting relies on non-reactive (by design) call to EventDownSyncCountsRepository.
 * To wrap it in a quasi-reactive way, that call is triggered
 * every 5 minutes while there are subscribers to invoke().
 */
@Singleton
class EventDownSyncPeriodicCountUseCase @Inject constructor(
    private val eventDownSyncCountsRepository: EventDownSyncCountsRepository,
    @param:AppScope private val appScope: CoroutineScope,
) {
    private val sharedDownSyncCounts: SharedFlow<DownSyncCounts> = MutableSharedFlow<DownSyncCounts>(replay = 1).apply {
        appScope.launch {
            emit(eventDownSyncCountsRepository.countEventsToDownload()) // initial count
            subscriptionCount.map { it > 0}.distinctUntilChanged().collectLatest { hasSubscribers ->
                val isEligibleToPeriodicCounting =
                    hasSubscribers && isActive
                while (isEligibleToPeriodicCounting) {
                    emit(eventDownSyncCountsRepository.countEventsToDownload())
                    delay(DOWN_SYNC_COUNT_INTERVAL_MILLIS)
                }
            }
        }
    }.asSharedFlow()

    operator fun invoke(): Flow<DownSyncCounts> = sharedDownSyncCounts

    companion object {
        internal const val DOWN_SYNC_COUNT_INTERVAL_MILLIS = 300_000L
    }
}
