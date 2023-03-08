package com.simprints.infra.eventsync.sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.enrolment.records.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.event.domain.models.subject.*
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.eventsync.EventSyncRepository
import com.simprints.infra.eventsync.SampleSyncScopes
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.infra.eventsync.sync.down.EventDownSyncHelperImpl.Companion.EVENTS_BATCH_SIZE
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventDownSyncHelperImplTest {

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
            "moduleId",
            "attendantId",
            listOf(FaceReference("id", listOf(FaceTemplate("template"))))
        )
        val ENROLMENT_RECORD_MOVE = EnrolmentRecordMoveEvent(
            EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID_2,
                "attendantId",
                listOf(FaceReference("id", listOf(FaceTemplate("template"))))
            ),
            EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove(
                "subjectId",
                "projectId",
                DEFAULT_MODULE_ID,
                "attendantId",
            )
        )
    }

    private val projectOp = SampleSyncScopes.projectDownSyncScope.operations.first()
    private val moduleOp = SampleSyncScopes.modulesDownSyncScope.operations.first()

    private lateinit var downloadEventsChannel: Channel<EnrolmentRecordEvent>

    private lateinit var eventDownSyncHelper: EventDownSyncHelper

    @MockK
    private lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    private lateinit var eventSyncRepository: EventSyncRepository

    @MockK
    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var configManager: ConfigManager
    private val subjectFactory = SubjectFactoryImpl(EncodingUtilsImplForTests)


    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        eventDownSyncHelper = EventDownSyncHelperImpl(
            enrolmentRecordManager,
            eventSyncRepository,
            eventDownSyncScopeRepository,
            subjectFactory,
            configManager,
            timeHelper,
        )

        runTest(StandardTestDispatcher()) {
            mockProgressEmission(emptyList())
        }
    }

    @Test
    fun countForDownSync_shouldReturnRepoEventChannel() {
        runTest(StandardTestDispatcher()) {
            eventDownSyncHelper.countForDownSync(projectOp)
            coVerify { eventSyncRepository.countEventsToDownload(any()) }
        }
    }

    @Test
    fun downSync_shouldConsumeRepoEventChannel() {
        runTest(UnconfinedTestDispatcher()) {
            eventDownSyncHelper.downSync(this, projectOp)

            coVerify { eventSyncRepository.downloadEvents(this@runTest, projectOp.queryEvent) }
        }
    }

    @Test
    fun downSync_shouldProgressEventsInBatches() {
        runTest(StandardTestDispatcher()) {
            val eventsToDownload = mutableListOf<EnrolmentRecordEvent>()
            repeat(2 * EVENTS_BATCH_SIZE) { eventsToDownload += ENROLMENT_RECORD_DELETION }
            mockProgressEmission(eventsToDownload)

            val channel = eventDownSyncHelper.downSync(this, projectOp)

            val progress = channel.consumeAsFlow().toList()
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
    }

    @Test
    fun downSync_shouldEmitAFailureIfDownloadFailsAndThrowsAnException() {
        runTest(StandardTestDispatcher()) {
            coEvery {
                eventSyncRepository.downloadEvents(
                    any(),
                    any()
                )
            } throws Throwable("IO Exception")

            assertThrows<Throwable> {
                val channel = eventDownSyncHelper.downSync(this, projectOp)

                val progress = channel.consumeAsFlow().toList()
                assertThat(progress.last().operation.state).isEqualTo(FAILED)
                coVerify(exactly = 2) { eventDownSyncScopeRepository.insertOrUpdate(any()) }
            }
        }
    }

    @Test
    fun downSync_shouldProcessRecordCreationEvent() {
        runTest(StandardTestDispatcher()) {
            val event = ENROLMENT_RECORD_CREATION
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, projectOp).consumeAsFlow().toList()

            coVerify {
                enrolmentRecordManager.performActions(
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
    }

    @Test
    fun downSync_shouldProcessRecordDeletionEvent() {
        runTest(StandardTestDispatcher()) {
            val event = ENROLMENT_RECORD_DELETION
            mockProgressEmission(listOf(event))

            eventDownSyncHelper.downSync(this, projectOp).consumeAsFlow().toList()

            coVerify { enrolmentRecordManager.performActions(listOf(Deletion(event.payload.subjectId))) }
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theOriginalModuleSyncShouldDoNothing() {
        runTest(StandardTestDispatcher()) {
            val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
            mockProgressEmission(listOf(eventToMoveToModule2))
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                ""
            )

            eventDownSyncHelper.downSync(this, moduleOp).consumeAsFlow().toList()

            coVerify {
                enrolmentRecordManager.performActions(emptyList())
            }
        }
    }

    @Test
    fun moveSubjectFromModulesUnderSyncing_theDestinationModuleSyncShouldPerformCreation() {
        runTest(StandardTestDispatcher()) {
            val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
            mockProgressEmission(listOf(eventToMoveToModule2))
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2),
                ""
            )

            val syncByModule2 = moduleOp.copy(
                queryEvent = moduleOp.queryEvent.copy(
                    moduleIds = listOf(DEFAULT_MODULE_ID_2)
                )
            )
            eventDownSyncHelper.downSync(this, syncByModule2).consumeAsFlow().toList()

            coVerify {
                enrolmentRecordManager.performActions(
                    listOf(
                        Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation))
                    )
                )
            }
        }
    }

    @Test
    fun moveSubjectToAModuleNotUnderSyncing_shouldPerformDeletionOnly() {
        runTest(StandardTestDispatcher()) {
            val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
            mockProgressEmission(listOf(eventToMoveToModule2))
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(DEFAULT_MODULE_ID),
                ""
            )

            eventDownSyncHelper.downSync(this, moduleOp).consumeAsFlow().toList()

            coVerify {
                enrolmentRecordManager.performActions(
                    listOf(
                        Deletion(eventToMoveToModule2.payload.enrolmentRecordDeletion.subjectId)
                    )
                )
            }
        }
    }

    @Test
    fun moveSubjectToAModuleUnderSyncing_shouldPerformCreationOnly() {
        runTest(StandardTestDispatcher()) {
            val eventToMoveToModule2 = ENROLMENT_RECORD_MOVE
            mockProgressEmission(listOf(eventToMoveToModule2))
            coEvery { configManager.getDeviceConfiguration() } returns DeviceConfiguration(
                "",
                listOf(DEFAULT_MODULE_ID_2),
                ""
            )

            val syncByModule2 = moduleOp.copy(
                queryEvent = moduleOp.queryEvent.copy(
                    moduleIds = listOf(DEFAULT_MODULE_ID_2)
                )
            )
            eventDownSyncHelper.downSync(this, syncByModule2).consumeAsFlow().toList()

            coVerify {
                enrolmentRecordManager.performActions(
                    listOf(
                        Creation(subjectFactory.buildSubjectFromMovePayload(eventToMoveToModule2.payload.enrolmentRecordCreation))
                    )
                )
            }
        }
    }

    private suspend fun mockProgressEmission(progressEvents: List<EnrolmentRecordEvent>) {
        downloadEventsChannel = Channel(capacity = Channel.UNLIMITED)
        coEvery { eventSyncRepository.downloadEvents(any(), any()) } returns downloadEventsChannel

        progressEvents.forEach {
            downloadEventsChannel.send(it)
        }
        downloadEventsChannel.close()
    }
}
