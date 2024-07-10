package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.FAILED
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.RUNNING
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncResult
import com.simprints.infra.eventsync.sync.down.tasks.EventDownSyncTask.Companion.EVENTS_BATCH_SIZE
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class EventDownSyncTaskTest {

    companion object {

        val ENROLMENT_RECORD_DELETION = EnrolmentRecordDeletionEvent(
            "subjectId",
            "projectId",
            "moduleId",
            "attendantId",
        )
        val ENROLMENT_RECORD_CREATION = EnrolmentRecordCreationEvent(
            "subjectId",
            "projectId",
            "moduleId".asTokenizableRaw(),
            "attendantId".asTokenizableRaw(),
            listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
        )
        val ENROLMENT_RECORD_MOVE = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID_2,
                "attendantId".asTokenizableRaw(),
                listOf(FaceReference("id", listOf(FaceTemplate("template")), "format"))
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID,
                "attendantId".asTokenizableRaw(),
            )
        )
    }

    private val projectOp = SampleSyncScopes.projectDownSyncScope.operations.first()
    private val moduleOp = SampleSyncScopes.modulesDownSyncScope.operations.first()

    private lateinit var downloadEventsChannel: Channel<EnrolmentRecordEvent>

    private lateinit var eventDownSyncTask: EventDownSyncTask

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var eventRemoteDataSource: EventRemoteDataSource

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var eventScope: EventScope

    private lateinit var subjectFactory: SubjectFactory

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        subjectFactory = SubjectFactory(
            encodingUtils = EncodingUtilsImplForTests,
            timeHelper = timeHelper,
        )
        eventDownSyncTask = EventDownSyncTask(
            enrolmentRecordRepository,
            eventDownSyncScopeRepository,
            subjectFactory,
            configManager,
            timeHelper,
            eventRemoteDataSource,
            eventRepository,
        )
    }

    @Test
    fun downSync_shouldProgressEventsInBatches() = runTest {
        val eventsToDownload = mutableListOf<EnrolmentRecordEvent>()
        repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += ENROLMENT_RECORD_DELETION }
        mockProgressEmission(eventsToDownload)

        val progress = eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        assertThat(progress.first().progress).isEqualTo(1)
        assertThat(progress.first().operation.state).isEqualTo(RUNNING)
        //Shifted by 1 since the first batch is immediately emitted with only 1 element
        assertThat(progress[1].progress).isEqualTo(EVENTS_BATCH_SIZE + 2)
        assertThat(progress[1].operation.state).isEqualTo(RUNNING)
        assertThat(progress[2].progress).isEqualTo(2 * EVENTS_BATCH_SIZE)
        assertThat(progress[2].operation.state).isEqualTo(RUNNING)
        assertThat(progress[3].operation.state).isEqualTo(COMPLETE)
        coVerify(exactly = 4) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test
    fun downSync_shouldAddEventToProvidedScope() = runTest {
        val eventsToDownload = mutableListOf<EnrolmentRecordEvent>()
        repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += ENROLMENT_RECORD_DELETION }
        mockProgressEmission(eventsToDownload)

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        UUID.fromString(it.payload.requestId) != null &&
                        it.payload.eventsRead == eventsToDownload.size &&
                        it.payload.responseStatus == 200
                }
            )
        }
    }

    @Test
    fun downSync_shouldNotAddEventToProvidedScopeIfNoEvents() = runTest {
        mockProgressEmission(emptyList())

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify(exactly = 0) {
            eventRepository.addOrUpdateEvent(any(), any())
        }
    }

    @Test
    fun downSync_shouldEmitAFailureIfDownloadFails() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws Throwable("IO Exception")

        val progress = eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        assertThat(progress.last().operation.state).isEqualTo(FAILED)
        coVerify(exactly = 2) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test(expected = RemoteDbNotSignedInException::class)
    fun downSync_shouldThrowUpIfRemoteDbNotSignedInExceptionOccurs() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws RemoteDbNotSignedInException()

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()
    }

    @Test
    fun downSync_shouldAddEventWithErrorIfDownloadFails() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws Throwable("IO Exception")
        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent && !it.payload.errorType.isNullOrEmpty()
                }
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordCreationEvent() = runTest {
        val event = ENROLMENT_RECORD_CREATION
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Creation(
                        subjectFactory.buildSubjectFromCreationPayload(
                            event.payload
                        )
                    )
                )
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordDeletionEvent() = runTest {
        val event = ENROLMENT_RECORD_DELETION
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify { enrolmentRecordRepository.performActions(listOf(Deletion(event.payload.subjectId))) }
    }

    @Test
    fun downSync_shouldAddEventWithExceptionClassSimpleNameIfDownloadFails() = runTest {
        val expectedException = Exception("Test")
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws expectedException

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        it.payload.errorType == expectedException.javaClass.simpleName
                }
            )
        }
    }

    @Test
    fun downSync_shouldAddEventWithExceptionClassSimpleNameIfEventStreamFails() = runTest {
        val expectedException = CancellationException("Test")
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } returns EventDownSyncResult(
            0,
            status = 200,
            downloadEventsChannel
        )
        downloadEventsChannel.cancel(expectedException)

        eventDownSyncTask.downSync(this, projectOp, eventScope).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        it.payload.errorType == expectedException.javaClass.simpleName
                }
            )
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theOriginalModuleSyncShouldDoNothing() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            ""
        )

        eventDownSyncTask.downSync(this, moduleOp, eventScope).toList()

        coVerify {
            enrolmentRecordRepository.performActions(emptyList())
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theDestinationModuleSyncShouldPerformCreation() =
        runTest {
            val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
            mockProgressEmission(listOf(eventToMoveToModule2))
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                ""
            )

            val syncByModule2 = moduleOp.copy(
                queryEvent = moduleOp.queryEvent.copy(
                    moduleId = DEFAULT_MODULE_ID_2.value
                )
            )
            eventDownSyncTask.downSync(this, syncByModule2, eventScope).toList()

            coVerify {
                enrolmentRecordRepository.performActions(
                    listOf(
                        Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation))
                    )
                )
            }
        }

    @Test
    fun moveSubjectToAModuleNotUnderSyncing_shouldPerformDeletionOnly() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            language = "",
            selectedModules = listOf(DEFAULT_MODULE_ID),
            lastInstructionId = ""
        )

        eventDownSyncTask.downSync(this, moduleOp, eventScope).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId)
                )
            )
        }
    }

    @Test
    fun moveSubjectToAModuleUnderSyncing_shouldPerformCreationOnly() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID_2),
            ""
        )

        val syncByModule2 = moduleOp.copy(
            queryEvent = moduleOp.queryEvent.copy(
                moduleId = DEFAULT_MODULE_ID_2.value
            )
        )
        eventDownSyncTask.downSync(this, syncByModule2, eventScope).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation))
                )
            )
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EnrolmentRecordEvent>) {
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } returns EventDownSyncResult(
            0,
            status = 200,
            downloadEventsChannel
        )

        progressEvents.forEach {
            downloadEventsChannel.send(it)
        }
        downloadEventsChannel.close()
    }
}
