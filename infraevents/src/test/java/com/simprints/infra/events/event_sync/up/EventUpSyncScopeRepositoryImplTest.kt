package com.simprints.infra.events.event_sync.up

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.events_sync.up.EventUpSyncScopeRepository
import com.simprints.infra.events.events_sync.up.EventUpSyncScopeRepositoryImpl
import com.simprints.infra.events.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.infra.events.events_sync.up.domain.getUniqueKey
import com.simprints.infra.events.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.infra.events.events_sync.up.local.DbEventsUpSyncOperationState
import com.simprints.infra.events.sampledata.SampleDefaults
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.events.sampledata.SampleDefaults.projectUpSyncScope
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventUpSyncScopeRepositoryImplTest {

    @MockK
    lateinit var loginManager: LoginManager

    @MockK
    lateinit var upSyncOperationOperationDao: DbEventUpSyncOperationStateDao

    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventUpSyncScopeRepository =
            EventUpSyncScopeRepositoryImpl(
                loginManager,
                upSyncOperationOperationDao,
            )

        every { loginManager.getSignedInProjectIdOrEmpty() } returns SampleDefaults.DEFAULT_PROJECT_ID
        coEvery { upSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun `test delete all`() = runBlocking {
        //when
        eventUpSyncScopeRepository.deleteAll()
        // Then
        coVerify { upSyncOperationOperationDao.deleteAll() }
    }

    @Test
    fun `test insertOrUpdate shouldInsertIntoTheDb`() = runBlocking {
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
                TIME1
            )
        )
    }
}
