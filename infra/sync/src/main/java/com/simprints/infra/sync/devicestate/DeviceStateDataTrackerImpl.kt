package com.simprints.infra.sync.devicestate

import com.simprints.core.AppScope
import com.simprints.core.domain.sync.SyncOutcome
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.sync.ImageSyncTimestampProvider
import com.simprints.infra.sync.devicestate.internal.ObserveEnrolmentRecordsCountUseCase
import com.simprints.infra.sync.devicestate.internal.ObserveSamplesToUploadCountUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DeviceStateDataTrackerImpl @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val eventRepository: EventRepository,
    private val imageRepository: ImageRepository,
    private val eventSyncCache: EventSyncCache,
    private val imageSyncTimestampProvider: ImageSyncTimestampProvider,
    private val observeEnrolmentRecordsCount: ObserveEnrolmentRecordsCountUseCase,
    private val observeSamplesToUploadCount: ObserveSamplesToUploadCountUseCase,
    @param:AppScope private val appScope: CoroutineScope,
) : DeviceStateDataTracker {
    /** Bumped by [refresh] to re-subscribe every source; see [deferred]. */
    private val refreshes = MutableStateFlow(0)

    private val sharedState: SharedFlow<DeviceDataState> by lazy {
        combine(
            deferred(PROJECT_ID) { observeProjectId() },
            deferred(RECORD_COUNT) { observeEnrolmentRecordsCount() },
            deferred(PENDING_SAMPLES) { observeSamplesToUploadCount() },
            observeEventCounts(),
            observeLastSyncs(),
        ) { projectId, records, samples, eventCounts, (lastEventSync, lastSampleSync) ->
            DeviceDataState(
                projectId = projectId,
                recordCount = records,
                pendingScopes = eventCounts.pendingScopes,
                pendingEvents = eventCounts.pendingEvents,
                pendingEnrolments = eventCounts.pendingEnrolments,
                pendingSamples = samples,
                lastEventSyncAt = lastEventSync?.timestamp,
                lastEventSyncFailure = lastEventSync?.failure,
                lastSampleSyncAt = lastSampleSync?.timestamp,
                lastSampleSyncFailure = lastSampleSync?.failure,
            )
        }.shareIn(
            appScope,
            SharingStarted.WhileSubscribed(),
            replay = 1,
        )
    }

    override fun observeDeviceDataState(): Flow<DeviceDataState> = sharedState

    override fun refresh() {
        refreshes.update { it + 1 }
    }

    /**
     * Evaluates every source directly. Deliberately NOT [observeDeviceDataState] `.first()`, which
     * on a `shareIn(replay = 1)` flow would hand back the last cached emission.
     */
    override suspend fun getCurrentDeviceDataState(): DeviceDataState {
        val config = readOrNull(PROJECT_ID) { configRepository.getProjectConfiguration() }
        val projectId = config?.projectId?.takeIf { it.isNotBlank() }
        val lastEventSync = readOrNull(LAST_EVENT_SYNC) { eventSyncCache.readLastSyncOutcome() }
        val lastSampleSync = readOrNull(LAST_SAMPLE_SYNC) { imageSyncTimestampProvider.getLastSyncOutcome() }

        return DeviceDataState(
            projectId = projectId,
            recordCount = when {
                config == null -> null // could not read
                projectId == null -> 0 // signed out / nothing yet
                else -> readOrNull(RECORD_COUNT) { countRecordsInProject(projectId) }
            },
            pendingScopes = readOrNull(PENDING_SCOPES) { eventRepository.observeClosedEventScopeCounts().first() },
            pendingEvents = readOrNull(PENDING_EVENTS) { countEvents() },
            pendingEnrolments = readOrNull(PENDING_ENROLMENTS) { countEnrolmentEvents() },
            pendingSamples = when {
                config == null -> null // could not read
                projectId == null -> 0 // signed out / nothing yet
                else -> readOrNull(PENDING_SAMPLES) { imageRepository.getNumberOfImagesToUpload(projectId) }
            },
            lastEventSyncAt = lastEventSync?.timestamp,
            lastEventSyncFailure = lastEventSync?.failure,
            lastSampleSyncAt = lastSampleSync?.timestamp,
            lastSampleSyncFailure = lastSampleSync?.failure,
        )
    }

    override suspend fun getRecordCount(): Int = countRecords()

    override suspend fun hasPendingEvents(): Boolean = countEvents() > 0

    private fun observeProjectId(): Flow<String?> =
        configRepository.observeProjectConfiguration().map { it.projectId.takeIf { id -> id.isNotBlank() } }.distinctUntilChanged()

    // distinctUntilChanged because Room invalidates the events table on every insert, and each
    // emission re-runs the combine transform - including the encrypted timestamp reads.
    private fun observeTotalEventCount(): Flow<Int> = eventRepository.observeEventCount(type = null).distinctUntilChanged()

    private fun observePendingEnrolmentCount(): Flow<Int> = combine(
        eventRepository.observeEventCount(EventType.ENROLMENT_V2),
        eventRepository.observeEventCount(EventType.ENROLMENT_V4),
    ) { enrolmentsV2, enrolmentsV4 -> enrolmentsV2 + enrolmentsV4 }.distinctUntilChanged()

    /** Every record on the device, whatever project it belongs to - see [getRecordCount]. */
    private suspend fun countRecords(): Int = enrolmentRecordRepository.count()

    private suspend fun countRecordsInProject(projectId: String): Int =
        enrolmentRecordRepository.count(EnrolmentRecordQuery(projectId = projectId))

    private suspend fun countEvents(): Int = eventRepository.observeEventCount(type = null).first()

    private suspend fun countEnrolmentEvents(): Int = eventRepository.observeEventCount(EventType.ENROLMENT_V2).first() + eventRepository
        .observeEventCount(EventType.ENROLMENT_V4)
        .first()

    private fun observeEventCounts(): Flow<EventCounts> = combine(
        deferred(PENDING_SCOPES) { eventRepository.observeClosedEventScopeCounts() },
        deferred(PENDING_EVENTS) { observeTotalEventCount() },
        deferred(PENDING_ENROLMENTS) { observePendingEnrolmentCount() },
    ) { scopes, events, enrolments -> EventCounts(scopes, events, enrolments) }

    private fun observeLastSyncs(): Flow<Pair<SyncOutcome?, SyncOutcome?>> = combine(
        deferred(LAST_EVENT_SYNC) { eventSyncCache.observeLastSyncOutcome() },
        deferred(LAST_SAMPLE_SYNC) { imageSyncTimestampProvider.observeLastSyncOutcome() },
    ) { event, sample -> event to sample }

    /**
     * Builds the source flow only once something collects, so that merely holding the tracker
     * touches no repository, and reports a failure - in construction or in the stream - as `null`
     * rather than killing the whole state flow.
     */
    private fun <T> deferred(
        source: String,
        block: () -> Flow<T>,
    ): Flow<T?> = refreshes.flatMapLatest { flow { emitAll(block()) }.nullOnFailure(source) }

    /**
     * Records a source failure as `null` rather than letting it abort the whole snapshot, or
     * degrade to a zero that would read as "this device holds nothing".
     */
    private inline fun <T> readOrNull(
        source: String,
        block: () -> T?,
    ): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (t: Throwable) {
        Simber.i("Could not read $source for device data state", t, tag = SYNC)
        null
    }

    private fun <T> Flow<T>.nullOnFailure(source: String): Flow<T?> {
        // Flow is covariant, so widening before catch{} is what lets the failure be emitted as null.
        val nullable: Flow<T?> = this
        return nullable.catch { cause ->
            Simber.i("Could not observe $source for device data state", cause, tag = SYNC)
            emit(null)
        }
    }

    private data class EventCounts(
        val pendingScopes: Map<EventScopeType, Int>?,
        val pendingEvents: Int?,
        val pendingEnrolments: Int?,
    )

    companion object {
        private const val PROJECT_ID = "project id"
        private const val RECORD_COUNT = "record count"
        private const val PENDING_SCOPES = "pending scopes"
        private const val PENDING_EVENTS = "pending events"
        private const val PENDING_ENROLMENTS = "pending enrolments"
        private const val PENDING_SAMPLES = "pending samples"
        private const val LAST_EVENT_SYNC = "last event sync outcome"
        private const val LAST_SAMPLE_SYNC = "last sample sync outcome"
    }
}
