package com.simprints.infra.eventsync.event.commcare.cache

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

@ExperimentalCoroutinesApi
class CommCareSyncCacheTest {

    private lateinit var commCareSyncDao: CommCareSyncDao
    private lateinit var commCareSyncCache: CommCareSyncCache

    @Before
    fun setUp() {
        commCareSyncDao = mockk<CommCareSyncDao>()
        commCareSyncCache = CommCareSyncCache(commCareSyncDao)
    }

    @Test
    fun `addSyncedCase should call dao insert with timestamp`() = runTest {
        // Arrange
        val caseId = "case-123"
        val simprintsId = "simprints-456"
        val timestamp = System.currentTimeMillis()
        val expectedEntity = SyncedCaseEntity(caseId, simprintsId, timestamp)
        coEvery { commCareSyncDao.insert(any()) } returns Unit

        // Act
        commCareSyncCache.addSyncedCase(SyncedCaseEntity(caseId, simprintsId, timestamp))

        // Assert
        coVerify { commCareSyncDao.insert(expectedEntity) }
    }

    @Test
    fun `getSimprintsId should return id when entity exists`() = runTest {
        // Arrange
        val caseId = "case-123"
        val expectedSimprintsId = "simprints-456"
        val entity = SyncedCaseEntity(caseId, expectedSimprintsId, 12345L) // timestamp is arbitrary here
        coEvery { commCareSyncDao.getByCaseId(caseId) } returns entity

        // Act
        val actualSimprintsId = commCareSyncCache.getSimprintsId(caseId)

        // Assert
        coVerify { commCareSyncDao.getByCaseId(caseId) }
        assertEquals(expectedSimprintsId, actualSimprintsId)
    }

    @Test
    fun `getSimprintsId should return null when entity does not exist`() = runTest {
        // Arrange
        val caseId = "case-unknown"
        coEvery { commCareSyncDao.getByCaseId(caseId) } returns null

        // Act
        val actualSimprintsId = commCareSyncCache.getSimprintsId(caseId)

        // Assert
        coVerify { commCareSyncDao.getByCaseId(caseId) }
        assertNull(actualSimprintsId)
    }

    @Test
    fun `removeSyncedCase should call dao deleteByCaseId`() = runTest {
        // Arrange
        val caseId = "case-to-delete"
        coEvery { commCareSyncDao.deleteByCaseId(caseId) } returns Unit

        // Act
        commCareSyncCache.removeSyncedCase(caseId)

        // Assert
        coVerify { commCareSyncDao.deleteByCaseId(caseId) }
    }

    @Test
    fun `getAllSyncedCases should return list of case entities`() = runTest {
        // Arrange
        val expectedEntities = listOf(
            SyncedCaseEntity("case1", "simId1", 1000L),
            SyncedCaseEntity("case2", "simId2", 2000L)
        )
        coEvery { commCareSyncDao.getAll() } returns expectedEntities

        // Act
        val actualEntities = commCareSyncCache.getAllSyncedCases()

        // Assert
        coVerify { commCareSyncDao.getAll() }
        assertEquals(expectedEntities, actualEntities)
    }

    @Test
    fun `getAllSyncedCases should return empty list when no entities`() = runTest {
        // Arrange
        coEvery { commCareSyncDao.getAll() } returns emptyList()

        // Act
        val actualEntities = commCareSyncCache.getAllSyncedCases()

        // Assert
        coVerify { commCareSyncDao.getAll() }
        assertEquals(emptyList<SyncedCaseEntity>(), actualEntities)
    }

    @Test
    fun `clearAllSyncedCases should call dao clearAll`() = runTest {
        // Arrange
        coEvery { commCareSyncDao.clearAll() } returns Unit

        // Act
        commCareSyncCache.clearAllSyncedCases()

        // Assert
        coVerify { commCareSyncDao.clearAll() }
    }
}
