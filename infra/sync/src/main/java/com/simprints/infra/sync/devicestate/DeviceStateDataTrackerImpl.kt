package com.simprints.infra.sync.devicestate

import com.simprints.core.AppScope
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EventType
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.sync.ImageSyncTimestampProvider
import com.simprints.infra.sync.usecase.internal.ObserveEnrolmentRecordsCountUseCase
import com.simprints.infra.sync.usecase.internal.ObserveSamplesToUploadCountUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

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
    private val sharedState: SharedFlow<DeviceDataState> by lazy {
        combine(
            observeProjectId().nullOnFailure(PROJECT_ID),
            observeEnrolmentRecordsCount().nullOnFailure(RECORD_COUNT),
            observeSamplesToUploadCount().nullOnFailure(PENDING_SAMPLES),
            eventRepository.observeClosedEventScopeCounts().nullOnFailure(PENDING_SCOPES),
            observePendingEventCounts().nullOnFailure(PENDING_EVENTS),
        ) { projectId, records, samples, scopes, events ->
            DeviceDataState(
                projectId = projectId,
                recordCount = records,
                pendingScopes = scopes,
                pendingEvents = events?.first,
                pendingEnrolments = events?.second,
                pendingSamples = samples,
                // Encrypted preferences have no change notification, so they are re-read per emission.
                lastEventSyncAt = readLastEventSyncAt(),
                lastSampleSyncAt = readLastSampleSyncAt(),
            )
        }.shareIn(
            appScope,
            SharingStarted.WhileSubscribed(),
            replay = 1,
        )
    }

    override fun observeDeviceDataState(): Flow<DeviceDataState> = sharedState

    /**
     * Evaluates every source directly. Deliberately NOT [observeDeviceDataState] `.first()`, which
     * on a `shareIn(replay = 1)` flow would hand back the last cached emission.
     */
    override suspend fun getCurrentDeviceDataState(): DeviceDataState {
        val config = readOrNull(PROJECT_ID) { configRepository.getProjectConfiguration() }
        val projectId = config?.projectId?.takeIf { it.isNotBlank() }

        return DeviceDataState(
            projectId = projectId,
            recordCount = when {
                config == null -> null // could not read
                projectId == null -> 0 // signed out / nothing yet
                else -> readOrNull(RECORD_COUNT) { countRecords() }
            },
            pendingScopes = readOrNull(PENDING_SCOPES) { eventRepository.observeClosedEventScopeCounts().first() },
            pendingEvents = readOrNull(PENDING_EVENTS) { countEvents() },
            pendingEnrolments = readOrNull(PENDING_ENROLMENTS) { countEnrolmentEvents() },
            pendingSamples = when {
                config == null -> null // could not read
                projectId == null -> 0 // signed out / nothing yet
                else -> readOrNull(PENDING_SAMPLES) { imageRepository.getNumberOfImagesToUpload(projectId) }
            },
            lastEventSyncAt = readLastEventSyncAt(),
            lastSampleSyncAt = readLastSampleSyncAt(),
        )
    }

    override suspend fun getRecordCount(): Int = countRecords()

    override suspend fun hasPendingEvents(): Boolean = countEvents() > 0

    private suspend fun currentProjectId(): String? = configRepository
        .getProjectConfiguration()
        .projectId
        .takeIf { it.isNotBlank() }

    private fun observeProjectId(): Flow<String?> = configRepository
        .observeProjectConfiguration()
        .map { it.projectId.takeIf { id -> id.isNotBlank() } }
        .distinctUntilChanged()

    private fun observePendingEventCounts(): Flow<Pair<Int, Int>> = combine(
        eventRepository.observeEventCount(type = null),
        eventRepository.observeEventCount(EventType.ENROLMENT_V2),
        eventRepository.observeEventCount(EventType.ENROLMENT_V4),
    ) { events, enrolmentsV2, enrolmentsV4 -> events to (enrolmentsV2 + enrolmentsV4) }

    private suspend fun countRecords(): Int = enrolmentRecordRepository.count()

    private suspend fun countEvents(): Int = eventRepository.observeEventCount(type = null).first()

    private suspend fun countEnrolmentEvents(): Int = eventRepository.observeEventCount(EventType.ENROLMENT_V2).first() +
        eventRepository.observeEventCount(EventType.ENROLMENT_V4).first()

    private suspend fun readLastEventSyncAt(): Long? = readOrNull(LAST_EVENT_SYNC) {
        eventSyncCache.readLastSuccessfulSyncTime()?.ms
    }

    private fun readLastSampleSyncAt(): Long? = readOrNull(LAST_SAMPLE_SYNC) {
        imageSyncTimestampProvider.getLastImageSyncTimestamp()
    }

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
        // Flow is covariant, so widening before retryWhen{} is what lets the failure be emitted as null.
        val nullable: Flow<T?> = this
        return nullable.retryWhen { cause, _ ->
            if (cause is CancellationException) return@retryWhen false
            Simber.i("Could not observe $source for device data state", cause, tag = SYNC)
            // Report the gap as null, then re-subscribe: catch{} would end the flow, leaving this
            // source dark for as long as anything keeps collecting the shared state.
            emit(null)
            delay(SOURCE_RETRY_DELAY_MS.milliseconds)
            true
        }
    }

    companion object {
        private const val PROJECT_ID = "project id"
        private const val RECORD_COUNT = "record count"
        private const val PENDING_SCOPES = "pending scopes"
        private const val PENDING_EVENTS = "pending events"
        private const val PENDING_ENROLMENTS = "pending enrolments"
        private const val PENDING_SAMPLES = "pending samples"
        private const val LAST_EVENT_SYNC = "last event sync time"
        private const val LAST_SAMPLE_SYNC = "last sample sync time"

        private const val SOURCE_RETRY_DELAY_MS = 2_000L
    }
}
