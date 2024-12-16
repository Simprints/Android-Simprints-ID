package com.simprints.infra.eventsync.status.up

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.events.sampledata.SampleDefaults
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.eventsync.SampleSyncScopes.projectUpSyncScope
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.eventsync.status.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.eventsync.status.up.local.DbEventsUpSyncOperationState
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EventUpSyncScopeRepositoryTest {
    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var upSyncOperationOperationDao: DbEventUpSyncOperationStateDao

    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventUpSyncScopeRepository =
            EventUpSyncScopeRepository(
                authStore,
                upSyncOperationOperationDao,
            )

        every { authStore.signedInProjectId } returns SampleDefaults.DEFAULT_PROJECT_ID
        coEvery { upSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun `test delete all`() = runTest {
        // when
        eventUpSyncScopeRepository.deleteAll()
        // Then
        coVerify { upSyncOperationOperationDao.deleteAll() }
    }

    @Test
    fun `test insertOrUpdate shouldInsertIntoTheDb`() = runTest {
        eventUpSyncScopeRepository.insertOrUpdate(projectUpSyncScope.operation)

        coVerify { upSyncOperationOperationDao.insertOrUpdate(any()) }
    }

    @Test
    fun buildProjectUpSyncScope() {
        runTest(UnconfinedTestDispatcher()) {
            val syncScope = eventUpSyncScopeRepository.getUpSyncScope()

            coVerify { upSyncOperationOperationDao.load() }
            val op = syncScope.operation
            assertThat(op.lastState).isEqualTo(COMPLETE)
            assertThat(op.lastSyncTime).isEqualTo(TIME1)
        }
    }

    private fun getSyncOperationsWithLastResult(): List<DbEventsUpSyncOperationState> {
        val op = projectUpSyncScope.operation
        return listOf(
            DbEventsUpSyncOperationState(
                op.getUniqueKey(),
                COMPLETE,
                TIME1,
            ),
        )
    }
}
