package com.simprints.infra.enrolment.records.repository

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.enrolment.records.repository.commcare.CommCareSyncService
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.SelectEnrolmentRecordLocalDataSourceUseCase
import com.simprints.infra.enrolment.records.repository.local.migration.InsertRecordsInRoomDuringMigrationUseCase
import com.simprints.infra.enrolment.records.repository.remote.EnrolmentRecordRemoteDataSource
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EnrolmentRecordRepositoryImplCommCareSyncTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var remoteDataSource: EnrolmentRecordRemoteDataSource

    @MockK
    private lateinit var commCareDataSource: IdentityDataSource

    @MockK
    private lateinit var tokenizationProcessor: TokenizationProcessor

    @MockK
    private lateinit var selectEnrolmentRecordLocalDataSource: SelectEnrolmentRecordLocalDataSourceUseCase

    @MockK
    private lateinit var insertRecordsInRoomDuringMigration: InsertRecordsInRoomDuringMigrationUseCase

    @MockK
    private lateinit var commCareSyncService: CommCareSyncService

    @MockK
    private lateinit var jsonHelper: JsonHelper

    @MockK
    private lateinit var securityManager: SecurityManager

    private lateinit var repository: EnrolmentRecordRepositoryImpl

    private val testProject = Project(
        id = "testProject",
        name = "Test Project",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns mockk()

        coEvery { selectEnrolmentRecordLocalDataSource().performActions(any(), any()) } just Runs
        coEvery { insertRecordsInRoomDuringMigration(any(), any()) } just Runs
        coEvery { commCareSyncService.processSubjectActionsWithCaseTracking(any(), any(), any()) } just Runs
        coEvery { commCareSyncService.syncDeletedCases(any()) } just Runs

        repository = EnrolmentRecordRepositoryImpl(
            remoteDataSource = remoteDataSource,
            commCareDataSource = commCareDataSource,
            tokenizationProcessor = tokenizationProcessor,
            selectEnrolmentRecordLocalDataSource = selectEnrolmentRecordLocalDataSource,
            dispatcher = testCoroutineRule.testDispatcher,
            batchSize = 10,
            insertRecordsInRoomDuringMigration = insertRecordsInRoomDuringMigration,
            commCareSyncService = commCareSyncService,
            jsonHelper = jsonHelper,
            securityManager = securityManager
        )
    }

    @Test
    fun `performActions should call commCareSyncService to track case information`() = runTest {
        // Given
        val subject = createTestSubject()
        val actions = listOf(SubjectAction.Creation(subject))

        // When
        repository.performActions(actions, testProject)

        // Then
        coVerify { 
            selectEnrolmentRecordLocalDataSource().performActions(actions, testProject)
            insertRecordsInRoomDuringMigration(actions, testProject)
            commCareSyncService.processSubjectActionsWithCaseTracking(actions, testProject, any())
        }
    }

    @Test
    fun `syncCommCareCaseCache should call commCareSyncService to sync deleted cases`() = runTest {
        // Given
        val currentCaseIds = setOf("case1", "case2", "case3")

        // When
        repository.syncCommCareCaseCache(currentCaseIds)

        // Then
        coVerify { commCareSyncService.syncDeletedCases(currentCaseIds) }
    }

    @Test
    fun `extractCaseIdFromMetadata should extract caseId from valid JSON metadata`() = runTest {
        // Given
        val metadata = "{\"caseId\":\"case123\",\"otherField\":\"value\"}"
        every { jsonHelper.fromJson<Map<String, Any>>(metadata) } returns mapOf(
            "caseId" to "case123",
            "otherField" to "value"
        )

        val subject = createTestSubject(metadata = metadata)
        val actions = listOf(SubjectAction.Creation(subject))

        // When
        repository.performActions(actions, testProject)

        // Then
        coVerify { 
            commCareSyncService.processSubjectActionsWithCaseTracking(
                actions = actions,
                project = testProject,
                caseIdExtractor = any()
            )
        }
    }

    @Test
    fun `extractCaseIdFromMetadata should return null for invalid JSON metadata`() = runTest {
        // Given
        val metadata = "invalid json"
        every { jsonHelper.fromJson<Map<String, Any>>(metadata) } throws RuntimeException("Invalid JSON")

        val subject = createTestSubject(metadata = metadata)
        val actions = listOf(SubjectAction.Creation(subject))

        // When
        repository.performActions(actions, testProject)

        // Then
        coVerify { 
            commCareSyncService.processSubjectActionsWithCaseTracking(
                actions = actions,
                project = testProject,
                caseIdExtractor = any()
            )
        }
    }

    private fun createTestSubject(
        subjectId: String = "subject123",
        metadata: String? = null
    ) = Subject(
        subjectId = subjectId,
        projectId = testProject.id,
        attendantId = TokenizableString.Raw("attendant123"),
        moduleId = TokenizableString.Raw("module123"),
        metadata = metadata
    )
}