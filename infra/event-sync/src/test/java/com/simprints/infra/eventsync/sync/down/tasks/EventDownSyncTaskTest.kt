package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID_2
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

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
        val ENROLMENT_RECORD_MOVE_MODULE = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID_2,
                "attendantId".asTokenizableRaw(),
                listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID,
                "attendantId".asTokenizableRaw(),
            ),
        )
        val ENROLMENT_RECORD_MOVE_ATTENDANT = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId".asTokenizableRaw(),
                DEFAULT_USER_ID,
                listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId".asTokenizableRaw(),
                DEFAULT_USER_ID_2,
            ),
        )
        val ENROLMENT_RECORD_MOVE_ATTENDANT2 = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                "moduleId".asTokenizableRaw(),
                DEFAULT_USER_ID_2,
                listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                "moduleId".asTokenizableRaw(),
                DEFAULT_USER_ID,
            ),
        )
        val ENROLMENT_RECORD_UPDATE = EnrolmentRecordUpdateEvent(
            "subjectId",
            listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            listOf("referenceIdToDelete"),
        )
    }

    private val projectOp = SampleSyncScopes.projectDownSyncScope.operations.first()
    private val moduleOp = SampleSyncScopes.modulesDownSyncScope.operations.first()
    private val userOp = SampleSyncScopes.userDownSyncScope.operations.first()

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

    @MockK
    private lateinit var project: Project

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

        val progress = eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        assertThat(progress.first().progress).isEqualTo(1)
        assertThat(progress.first().operation.state).isEqualTo(RUNNING)
        // Shifted by 1 since the first batch is immediately emitted with only 1 element
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

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        UUID.fromString(it.payload.requestId) != null &&
                        it.payload.eventsRead == eventsToDownload.size &&
                        it.payload.responseStatus == 200
                },
            )
        }
    }

    @Test
    fun downSync_shouldNotAddEventToProvidedScopeIfNoEvents() = runTest {
        mockProgressEmission(emptyList())

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 0) {
            eventRepository.addOrUpdateEvent(any(), any())
        }
    }

    @Test
    fun downSync_shouldEmitAFailureIfDownloadFails() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws Throwable("IO Exception")

        val progress = eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        assertThat(progress.last().operation.state).isEqualTo(FAILED)
        coVerify(exactly = 2) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test(expected = RemoteDbNotSignedInException::class)
    fun downSync_shouldThrowUpIfRemoteDbNotSignedInExceptionOccurs() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws RemoteDbNotSignedInException()

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()
    }

    @Test
    fun downSync_shouldAddEventWithErrorIfDownloadFails() = runTest {
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws Throwable("IO Exception")
        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent && !it.payload.errorType.isNullOrEmpty()
                },
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordCreationEvent() = runTest {
        val event = ENROLMENT_RECORD_CREATION
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Creation(
                        subjectFactory.buildSubjectFromCreationPayload(
                            event.payload,
                        ),
                    ),
                ),
                project,
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordDeletionEvent() = runTest {
        val event = ENROLMENT_RECORD_DELETION
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify { enrolmentRecordRepository.performActions(listOf(Deletion(event.payload.subjectId)), project) }
    }

    @Test
    fun downSync_shouldAddEventWithExceptionClassSimpleNameIfDownloadFails() = runTest {
        val expectedException = Exception("Test")
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } throws expectedException

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        it.payload.errorType == expectedException.javaClass.simpleName
                },
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
            downloadEventsChannel,
        )
        downloadEventsChannel.cancel(expectedException)

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        it.payload.errorType == expectedException.javaClass.simpleName
                },
            )
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theOriginalModuleSyncShouldDoNothing() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            "",
        )

        eventDownSyncTask.downSync(this, moduleOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theDestinationModuleSyncShouldPerformCreation() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            "",
        )

        val syncByModule2 = moduleOp.copy(
            queryEvent = moduleOp.queryEvent.copy(
                moduleId = DEFAULT_MODULE_ID_2.value,
            ),
        )
        eventDownSyncTask.downSync(this, syncByModule2, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectToAModuleNotUnderSyncing_shouldPerformDeletionOnly() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            language = "",
            selectedModules = listOf(DEFAULT_MODULE_ID),
            lastInstructionId = "",
        )

        eventDownSyncTask.downSync(this, moduleOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectToAModuleUnderSyncing_shouldPerformCreation() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID_2),
            "",
        )

        val syncByModule2 = moduleOp.copy(
            queryEvent = moduleOp.queryEvent.copy(
                moduleId = DEFAULT_MODULE_ID_2.value,
            ),
        )
        eventDownSyncTask.downSync(this, syncByModule2, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectToModule2_syncModule1_shouldPerformCreationInModule2() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            "",
        )

        val syncByModule = moduleOp.copy(
            queryEvent = moduleOp.queryEvent.copy(
                moduleId = DEFAULT_MODULE_ID.value,
            ),
        )
        eventDownSyncTask.downSync(this, syncByModule, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectFromAttendantUnderSyncingToAnotherOne_ShouldDPerformDeleteOnly() = runTest {
        val eventToMoveToAttendant2 = ENROLMENT_RECORD_MOVE_ATTENDANT2
        mockProgressEmission(listOf(eventToMoveToAttendant2))

        eventDownSyncTask.downSync(this, userOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(Deletion(eventToMoveToAttendant2.payload.enrolmentRecordDeletion.subjectId)),
                project,
            )
        }
    }

    @Test
    fun moveSubjectFromAnotherAttendantToAttendantUnderSyncing_ShouldDPerformDeleteAndCreate() = runTest {
        val eventToMoveToAttendant2 = ENROLMENT_RECORD_MOVE_ATTENDANT
        mockProgressEmission(listOf(eventToMoveToAttendant2))

        eventDownSyncTask.downSync(this, userOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToAttendant2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToAttendant2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun moveSubjectUnderProjectSync_ShouldPerformDeleteAndCreate() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockProgressEmission(listOf(eventToMoveToModule2))

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(
                    Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId),
                    Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation)),
                ),
                project,
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordUpdateEvent_withCreations() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
                faceSamples = listOf(
                    FaceSample(byteArrayOf(), "format", "referenceId"),
                ),
            ),
        )

        val event = ENROLMENT_RECORD_UPDATE
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                withArg { actions -> actions.all { it is Creation } },
                any(),
            )
        }
    }

    @Test
    fun downSync_shouldProcessRecordUpdateEvent_withDeletions() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
                faceSamples = listOf(
                    FaceSample(byteArrayOf(), "format", "referenceIdToDelete"),
                ),
            ),
        )

        val event = ENROLMENT_RECORD_UPDATE
        mockProgressEmission(listOf(event))

        eventDownSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                withArg { actions -> actions.all { it is Deletion } },
                any(),
            )
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EnrolmentRecordEvent>) {
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventRemoteDataSource.getEvents(any(), any(), any()) } returns EventDownSyncResult(
            0,
            status = 200,
            downloadEventsChannel,
        )

        progressEvents.forEach {
            downloadEventsChannel.send(it)
        }
        downloadEventsChannel.close()
    }
}
