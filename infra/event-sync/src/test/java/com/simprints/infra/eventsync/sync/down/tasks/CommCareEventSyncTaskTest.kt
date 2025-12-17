package com.simprints.infra.eventsync.sync.down.tasks

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Deletion
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Update
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
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.CommCareEventSyncResult
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.FAILED
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.RUNNING
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import com.simprints.infra.eventsync.sync.down.tasks.BaseEventDownSyncTask.Companion.EVENTS_BATCH_SIZE
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class CommCareEventSyncTaskTest {
    companion object {
        val ENROLMENT_RECORD_DELETION = EnrolmentRecordDeletionEvent(
            "subjectId",
            "projectId",
            "moduleId",
            "attendantId",
        )
        val ENROLMENT_RECORD_CREATION = EnrolmentRecordCreationEvent(
            subjectId = "subjectId",
            projectId = "projectId",
            moduleId = "moduleId".asTokenizableRaw(),
            attendantId = "attendantId".asTokenizableRaw(),
            biometricReferences = listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            externalCredentials = listOf(
                ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        )
        val ENROLMENT_RECORD_MOVE_MODULE = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                subjectId = "subjectId",
                projectId = "projectId",
                moduleId = DEFAULT_MODULE_ID_2,
                attendantId = "attendantId".asTokenizableRaw(),
                biometricReferences = listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
                externalCredential = ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                subjectId = "subjectId",
                projectId = "projectId",
                moduleId = DEFAULT_MODULE_ID,
                attendantId = "attendantId".asTokenizableRaw(),
            ),
        )
        val ENROLMENT_RECORD_MOVE_ATTENDANT = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                subjectId = "subjectId",
                projectId = "projectId",
                moduleId = "moduleId".asTokenizableRaw(),
                attendantId = DEFAULT_USER_ID,
                biometricReferences = listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
                externalCredential = ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                subjectId = "subjectId",
                projectId = "projectId",
                moduleId = "moduleId".asTokenizableRaw(),
                attendantId = DEFAULT_USER_ID_2,
            ),
        )
        val ENROLMENT_RECORD_UPDATE = EnrolmentRecordUpdateEvent(
            subjectId = "subjectId",
            biometricReferencesAdded = listOf(FaceReference("id", listOf(FaceTemplate("template")), "format")),
            biometricReferencesRemoved = listOf("referenceIdToDelete"),
            externalCredentialsAdded = listOf(
                ExternalCredential(
                    id = "id",
                    value = "value".asTokenizableEncrypted(),
                    subjectId = "subjectId",
                    type = ExternalCredentialType.NHISCard,
                ),
            ),
        )
    }

    private val projectOp = SampleSyncScopes.projectDownSyncScope.operations.first()
    private val moduleOp = SampleSyncScopes.modulesDownSyncScope.operations.first()
    private val userOp = SampleSyncScopes.userDownSyncScope.operations.first()

    private lateinit var commCareEventSyncTask: CommCareEventSyncTask

    @MockK
    private lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var commCareEventDataSource: CommCareEventDataSource

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
        commCareEventSyncTask = CommCareEventSyncTask(
            enrolmentRecordRepository,
            eventDownSyncScopeRepository,
            subjectFactory,
            configManager,
            timeHelper,
            eventRepository,
            commCareEventDataSource,
        )
    }

    @Test
    fun downSync_shouldProgressEventsInBatches() = runTest {
        val eventsToDownload = mutableListOf<EnrolmentRecordEvent>()
        repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += ENROLMENT_RECORD_DELETION }
        mockCommCareDataSource(eventsToDownload)

        val progress = commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

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
        mockCommCareDataSource(eventsToDownload)

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 1) {
            eventRepository.addOrUpdateEvent(
                eventScope,
                match {
                    it is EventDownSyncRequestEvent &&
                        UUID.fromString(it.payload.requestId) != null &&
                        it.payload.eventsRead == eventsToDownload.size
                },
            )
        }
    }

    @Test
    fun downSync_shouldNotAddEventToProvidedScopeIfNoEvents() = runTest {
        mockCommCareDataSource(emptyList())

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify(exactly = 0) {
            eventRepository.addOrUpdateEvent(any(), any())
        }
    }

    @Test
    fun downSync_shouldEmitAFailureIfDownloadFails() = runTest {
        coEvery { commCareEventDataSource.getEvents(any()) } throws Throwable("CommCare Exception")

        val progress = commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        assertThat(progress.last().operation.state).isEqualTo(FAILED)
        coVerify(exactly = 2) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
    }

    @Test(expected = SecurityException::class)
    fun downSync_shouldThrowUpIfSecurityExceptionOccurs() = runTest {
        coEvery { commCareEventDataSource.getEvents(any()) } throws SecurityException("Security Exception")

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()
    }

    @Test(expected = IllegalStateException::class)
    fun downSync_shouldThrowUpIfIllegalStateExceptionOccurs() = runTest {
        coEvery { commCareEventDataSource.getEvents(any()) } throws IllegalStateException("Illegal State Exception")

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()
    }

    @Test
    fun downSync_shouldAddEventWithErrorIfDownloadFails() = runTest {
        coEvery { commCareEventDataSource.getEvents(any()) } throws Throwable("CommCare Exception")
        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

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
        mockCommCareDataSource(listOf(event))

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

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
        mockCommCareDataSource(listOf(event))

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify { enrolmentRecordRepository.performActions(listOf(Deletion(event.payload.subjectId)), project) }
    }

    @Test
    fun downSync_shouldAddEventWithExceptionClassSimpleNameIfDownloadFails() = runTest {
        val expectedException = Exception("Test")
        coEvery { commCareEventDataSource.getEvents(any()) } throws expectedException

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

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
    fun downSync_shouldProcessRecordUpdateEvent_withUpdate() = runTest {
        coEvery { enrolmentRecordRepository.load(any()) } returns listOf(
            Subject(
                subjectId = "subjectId",
                projectId = "projectId",
                attendantId = "moduleId".asTokenizableRaw(),
                moduleId = "attendantId".asTokenizableRaw(),
                samples = listOf(
                    Sample(
                        template = BiometricTemplate(
                            template = byteArrayOf(),
                        ),
                        format = "format",
                        referenceId = "referenceId",
                        modality = Modality.FACE,
                    ),
                ),
            ),
        )

        val event = ENROLMENT_RECORD_UPDATE
        mockCommCareDataSource(listOf(event))

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                match<List<SubjectAction>> { actions ->
                    actions.size == 1 && actions.first() is Update
                },
                any(),
            )
        }
    }

    @Test
    fun downSync_shouldCallOnEventsProcessedOnDataSource() = runTest {
        val eventsToDownload = listOf(ENROLMENT_RECORD_CREATION, ENROLMENT_RECORD_DELETION)
        mockCommCareDataSource(eventsToDownload)

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

        coVerify { commCareEventDataSource.onEventsProcessed(listOf(ENROLMENT_RECORD_CREATION)) }
        coVerify { commCareEventDataSource.onEventsProcessed(listOf(ENROLMENT_RECORD_DELETION)) }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_shouldPerformBothActions() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockCommCareDataSource(listOf(eventToMoveToModule2))
        coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
            "",
            listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
            "",
        )

        commCareEventSyncTask.downSync(this, moduleOp, eventScope, project).toList()

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
    fun moveSubjectFromAttendantUnderSyncingToAnotherOne_shouldPerformDeletionOnly() = runTest {
        val eventToMoveToAttendant = ENROLMENT_RECORD_MOVE_ATTENDANT
        mockCommCareDataSource(listOf(eventToMoveToAttendant))

        val userOpForUser2 = userOp.copy(
            queryEvent = userOp.queryEvent.copy(
                attendantId = DEFAULT_USER_ID_2.value,
            ),
        )
        commCareEventSyncTask.downSync(this, userOpForUser2, eventScope, project).toList()

        coVerify {
            enrolmentRecordRepository.performActions(
                listOf(Deletion(eventToMoveToAttendant.payload.enrolmentRecordDeletion.subjectId)),
                project,
            )
        }
    }

    @Test
    fun moveSubjectUnderProjectSync_shouldPerformBothActions() = runTest {
        val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE_MODULE
        mockCommCareDataSource(listOf(eventToMoveToModule2))

        commCareEventSyncTask.downSync(this, projectOp, eventScope, project).toList()

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

    private fun mockCommCareDataSource(events: List<EnrolmentRecordEvent>) {
        coEvery { commCareEventDataSource.getEvents(any()) } returns CommCareEventSyncResult(
            totalCount = events.size,
            eventFlow = flowOf(*events.toTypedArray()),
        )
    }
}
