package com.simprints.infra.eventsync

import com.simprints.core.domain.common.Modality
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.scope.EventScopeEndCause
import com.simprints.infra.events.event.domain.models.scope.EventScopeType
import com.simprints.infra.eventsync.sync.down.tasks.CommCareEventSyncTask
import com.simprints.infra.eventsync.sync.down.tasks.SimprintsEventDownSyncTask
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class DownSyncSubjectUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var simprintsDownSyncTask: SimprintsEventDownSyncTask

    @MockK
    private lateinit var commCareDownSyncTask: CommCareEventSyncTask

    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var project: Project

    @MockK
    private lateinit var eventScope: EventScope

    @MockK
    private lateinit var extractCommCareCaseId: ExtractCommCareCaseIdUseCase

    private lateinit var useCase: DownSyncSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { configRepository.getProject() } returns project
        coEvery { configRepository.getProjectConfiguration() } returns createProjectConfiguration()
        coEvery { eventRepository.createEventScope(any()) } returns eventScope

        useCase = DownSyncSubjectUseCase(
            eventRepository = eventRepository,
            simprintsDownSyncTask = simprintsDownSyncTask,
            commCareSyncTask = commCareDownSyncTask,
            configRepository = configRepository,
            extractCommCareCaseId = extractCommCareCaseId,
            dispatcher = testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `uses CommCare down sync when CommCare config is present`() = runTest {
        val metadata = """{"caseId":"case123"}"""
        val caseId = "case123"
        val modalities = listOf(Modality.FINGERPRINT)
        coEvery {
            configRepository.getProjectConfiguration()
        } returns createProjectConfiguration(
            modalities = modalities,
            hasSimprints = false,
            hasCommCare = true,
        )
        every { extractCommCareCaseId(metadata) } returns caseId
        coEvery { commCareDownSyncTask.downSync(any(), any(), any(), any()) } returns emptyFlow()

        useCase(DEFAULT_PROJECT_ID, SUBJECT_ID, metadata)

        coVerify { eventRepository.createEventScope(EventScopeType.DOWN_SYNC) }
        coVerify {
            commCareDownSyncTask.downSync(
                any(),
                match {
                    it.queryEvent.projectId == DEFAULT_PROJECT_ID &&
                        it.queryEvent.subjectId == SUBJECT_ID &&
                        it.queryEvent.externalIds == listOf(caseId) &&
                        it.queryEvent.modes == modalities
                },
                eventScope,
                project,
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    @Test
    fun `uses CommCare down sync with null external ids when case id is missing`() = runTest {
        val metadata = """{"other":"value"}"""
        coEvery {
            configRepository.getProjectConfiguration()
        } returns createProjectConfiguration(
            hasSimprints = false,
            hasCommCare = true,
        )
        every { extractCommCareCaseId(metadata) } returns null
        coEvery { commCareDownSyncTask.downSync(any(), any(), any(), any()) } returns emptyFlow()

        useCase(DEFAULT_PROJECT_ID, SUBJECT_ID, metadata)

        coVerify {
            commCareDownSyncTask.downSync(
                any(),
                match { it.queryEvent.externalIds == null },
                eventScope,
                project,
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    @Test
    fun `uses Simprints down sync when Simprints config is present`() = runTest {
        val modalities = listOf(Modality.FACE)
        coEvery {
            configRepository.getProjectConfiguration()
        } returns createProjectConfiguration(
            modalities = modalities,
            hasSimprints = true,
            hasCommCare = false,
        )
        coEvery { simprintsDownSyncTask.downSync(any(), any(), any(), any()) } returns emptyFlow()

        useCase(DEFAULT_PROJECT_ID, SUBJECT_ID, "metadata")

        coVerify {
            simprintsDownSyncTask.downSync(
                any(),
                match {
                    it.queryEvent.projectId == DEFAULT_PROJECT_ID &&
                        it.queryEvent.subjectId == SUBJECT_ID &&
                        it.queryEvent.externalIds == null &&
                        it.queryEvent.modes == modalities
                },
                eventScope,
                project,
            )
        }
        coVerify { eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED) }
        coVerify(exactly = 0) { commCareDownSyncTask.downSync(any(), any(), any(), any()) }
        coVerify(exactly = 0) { extractCommCareCaseId(any()) }
    }

    @Test
    fun `closes scope without running tasks when down sync config is missing`() = runTest {
        coEvery {
            configRepository.getProjectConfiguration()
        } returns createProjectConfiguration(
            hasSimprints = false,
            hasCommCare = false,
        )

        useCase(DEFAULT_PROJECT_ID, SUBJECT_ID, "metadata")

        coVerify { eventRepository.createEventScope(EventScopeType.DOWN_SYNC) }
        coVerify { eventRepository.closeEventScope(eventScope, EventScopeEndCause.WORKFLOW_ENDED) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
        coVerify(exactly = 0) { commCareDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    @Test
    fun `returns early when project is missing`() = runTest {
        coEvery { configRepository.getProject() } returns null

        useCase(DEFAULT_PROJECT_ID, SUBJECT_ID, "metadata")

        coVerify(exactly = 0) { configRepository.getProjectConfiguration() }
        coVerify(exactly = 0) { eventRepository.createEventScope(any()) }
        coVerify(exactly = 0) { eventRepository.closeEventScope(any<EventScope>(), any()) }
        coVerify(exactly = 0) { simprintsDownSyncTask.downSync(any(), any(), any(), any()) }
        coVerify(exactly = 0) { commCareDownSyncTask.downSync(any(), any(), any(), any()) }
    }

    private fun createProjectConfiguration(
        modalities: List<Modality> = emptyList(),
        hasSimprints: Boolean = true,
        hasCommCare: Boolean = false,
    ): ProjectConfiguration = mockk {
        every { general.modalities } returns modalities
        every { synchronization.down.simprints } returns if (hasSimprints) mockk() else null
        every { synchronization.down.commCare } returns if (hasCommCare) mockk() else null
    }

    companion object {
        private const val DEFAULT_PROJECT_ID = "project-id"
        private const val SUBJECT_ID = "subject-id"
    }
}
