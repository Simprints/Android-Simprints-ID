package com.simprints.eventsystem.event_sync.up

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository
import com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.eventsystem.events_sync.up.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.eventsystem.sampledata.SampleDefaults
import com.simprints.eventsystem.sampledata.SampleDefaults.TIME1
import com.simprints.eventsystem.sampledata.SampleDefaults.projectUpSyncScope
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EventUpSyncScopeRepositoryImplTest {

    @MockK
    lateinit var loginInfoManager: LoginInfoManager

    @MockK
    lateinit var upSyncOperationOperationDao: DbEventUpSyncOperationStateDao

    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val testDispatcherProvider = object : DispatcherProvider {
        override fun main(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun default(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun io(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher

        override fun unconfined(): CoroutineDispatcher = testCoroutineRule.testCoroutineDispatcher
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventUpSyncScopeRepository =
            EventUpSyncScopeRepositoryImpl(
                loginInfoManager,
                upSyncOperationOperationDao,
                testDispatcherProvider
            )

        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns SampleDefaults.DEFAULT_PROJECT_ID
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

    private fun getSyncOperationsWithLastResult(): List<com.simprints.eventsystem.events_sync.up.local.DbEventsUpSyncOperationState> {
        val op = projectUpSyncScope.operation
        return listOf(
            com.simprints.eventsystem.events_sync.up.local.DbEventsUpSyncOperationState(
                op.getUniqueKey(),
                COMPLETE,
                TIME1
            )
        )
    }
}
