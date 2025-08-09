package com.simprints.infra.enrolment.records.repository.commcare

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.commcare.models.CommCareCaseSyncInfo
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CommCareSyncServiceTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var caseSyncCache: CommCareCaseSyncCache

    private lateinit var syncService: CommCareSyncService

    private val testProject = Project(
        id = "testProject",
        name = "Test Project",
        createdAt = "2023-01-01T00:00:00Z",
        updatedAt = "2023-01-01T00:00:00Z"
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        coEvery { caseSyncCache.saveCaseSyncInfo(any(), any()) } just Runs
        coEvery { caseSyncCache.updateCaseLastModified(any()) } just Runs
        coEvery { caseSyncCache.deleteCaseSyncInfo(any()) } just Runs
        coEvery { caseSyncCache.getAllCaseSyncInfo() } returns emptyMap()

        syncService = CommCareSyncService(caseSyncCache)
    }

    @Test
    fun `processSubjectActionsWithCaseTracking should save case info for creation actions with caseId`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val metadata = "{\"caseId\":\"$caseId\"}"
        val subject = createTestSubject(subjectId, metadata)
        val actions = listOf(SubjectAction.Creation(subject))
        val caseIdExtractor: (String?) -> String? = { meta ->
            if (meta?.contains("case123") == true) caseId else null
        }

        // When
        syncService.processSubjectActionsWithCaseTracking(actions, testProject, caseIdExtractor)

        // Then
        coVerify { caseSyncCache.saveCaseSyncInfo(caseId, subjectId) }
    }

    @Test
    fun `processSubjectActionsWithCaseTracking should not save case info for creation actions without caseId`() = runTest {
        // Given
        val subjectId = "subject456"
        val metadata = "{\"otherField\":\"value\"}"
        val subject = createTestSubject(subjectId, metadata)
        val actions = listOf(SubjectAction.Creation(subject))
        val caseIdExtractor: (String?) -> String? = { null } // No caseId found

        // When
        syncService.processSubjectActionsWithCaseTracking(actions, testProject, caseIdExtractor)

        // Then
        coVerify(exactly = 0) { caseSyncCache.saveCaseSyncInfo(any(), any()) }
    }

    @Test
    fun `processSubjectActionsWithCaseTracking should update case info for update actions`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val existingCaseInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val actions = listOf(
            SubjectAction.Update(
                subjectId = subjectId,
                faceSamplesToAdd = emptyList(),
                fingerprintSamplesToAdd = emptyList(),
                referenceIdsToRemove = emptyList()
            )
        )
        val caseIdExtractor: (String?) -> String? = { null } // Not used for updates

        coEvery { caseSyncCache.getAllCaseSyncInfo() } returns mapOf(caseId to existingCaseInfo)

        // When
        syncService.processSubjectActionsWithCaseTracking(actions, testProject, caseIdExtractor)

        // Then
        coVerify { caseSyncCache.updateCaseLastModified(caseId) }
    }

    @Test
    fun `processSubjectActionsWithCaseTracking should delete case info for deletion actions`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val existingCaseInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val actions = listOf(SubjectAction.Deletion(subjectId))
        val caseIdExtractor: (String?) -> String? = { null } // Not used for deletions

        coEvery { caseSyncCache.getAllCaseSyncInfo() } returns mapOf(caseId to existingCaseInfo)

        // When
        syncService.processSubjectActionsWithCaseTracking(actions, testProject, caseIdExtractor)

        // Then
        coVerify { caseSyncCache.deleteCaseSyncInfo(caseId) }
    }

    @Test
    fun `syncDeletedCases should delete cases not present in current CommCare cases`() = runTest {
        // Given
        val currentCases = setOf("case1", "case3") // case2 is missing
        val cachedCases = mapOf(
            "case1" to CommCareCaseSyncInfo("case1", "subject1", 111L),
            "case2" to CommCareCaseSyncInfo("case2", "subject2", 222L), // Should be deleted
            "case3" to CommCareCaseSyncInfo("case3", "subject3", 333L)
        )

        coEvery { caseSyncCache.getAllCaseSyncInfo() } returns cachedCases

        // When
        syncService.syncDeletedCases(currentCases)

        // Then
        coVerify { caseSyncCache.deleteCaseSyncInfo("case2") }
        coVerify(exactly = 0) { caseSyncCache.deleteCaseSyncInfo("case1") }
        coVerify(exactly = 0) { caseSyncCache.deleteCaseSyncInfo("case3") }
    }

    private fun createTestSubject(
        subjectId: String,
        metadata: String? = null
    ) = Subject(
        subjectId = subjectId,
        projectId = testProject.id,
        attendantId = TokenizableString.Raw("attendant123"),
        moduleId = TokenizableString.Raw("module123"),
        metadata = metadata
    )
}