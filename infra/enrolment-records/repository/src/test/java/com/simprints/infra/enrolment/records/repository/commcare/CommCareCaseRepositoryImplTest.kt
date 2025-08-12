package com.simprints.infra.enrolment.records.repository.commcare

import com.simprints.infra.enrolment.records.repository.commcare.domain.CommCareCase
import com.simprints.infra.enrolment.records.room.store.CommCareCaseDao
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.enrolment.records.room.store.models.DbCommCareCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CommCareCaseRepositoryImplTest {

    private lateinit var repository: CommCareCaseRepositoryImpl
    private val mockDatabaseFactory: SubjectsDatabaseFactory = mockk()
    private val mockDatabase: SubjectsDatabase = mockk()
    private val mockDao: CommCareCaseDao = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testCase = CommCareCase(
        caseId = "case123",
        subjectId = "subject456",
        lastModified = 1640995200000L,
    )

    private val testDbCase = DbCommCareCase(
        caseId = "case123",
        subjectId = "subject456",
        lastModified = 1640995200000L,
    )

    @Before
    fun setup() {
        every { mockDatabaseFactory.build() } returns mockDatabase
        every { mockDatabase.commCareCaseDao } returns mockDao
        
        repository = CommCareCaseRepositoryImpl(mockDatabaseFactory, testDispatcher)
    }

    @Test
    fun `saveCase should call dao insertOrUpdateCase`() = runTest {
        coEvery { mockDao.insertOrUpdateCase(any()) } returns Unit

        repository.saveCase(testCase)

        coVerify { mockDao.insertOrUpdateCase(testDbCase) }
    }

    @Test
    fun `getCase should return mapped domain case when found`() = runTest {
        coEvery { mockDao.getCase("case123") } returns testDbCase

        val result = repository.getCase("case123")

        assertEquals(testCase, result)
    }

    @Test
    fun `getCase should return null when not found`() = runTest {
        coEvery { mockDao.getCase("case123") } returns null

        val result = repository.getCase("case123")

        assertNull(result)
    }

    @Test
    fun `deleteCase should call dao deleteCase`() = runTest {
        coEvery { mockDao.deleteCase("case123") } returns Unit

        repository.deleteCase("case123")

        coVerify { mockDao.deleteCase("case123") }
    }

    @Test
    fun `deleteCaseBySubjectId should call dao deleteCaseBySubjectId`() = runTest {
        coEvery { mockDao.deleteCaseBySubjectId("subject456") } returns Unit

        repository.deleteCaseBySubjectId("subject456")

        coVerify { mockDao.deleteCaseBySubjectId("subject456") }
    }
}