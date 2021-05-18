package com.simprints.eventsystem.events_sync.up

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults
import com.simprints.id.sampledata.SampleDefaults.TIME1
import com.simprints.id.sampledata.SampleDefaults.projectUpSyncScope
import com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.eventsystem.events_sync.up.domain.getUniqueKey
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.sharedpreferences.PreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventUpSyncScopeRepositoryImplTest {

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var upSyncOperationOperationDao: com.simprints.eventsystem.events_sync.up.local.DbEventUpSyncOperationStateDao

    private lateinit var eventUpSyncScopeRepository: com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventUpSyncScopeRepository =
            com.simprints.eventsystem.events_sync.up.EventUpSyncScopeRepositoryImpl(
                loginInfoManager,
                upSyncOperationOperationDao
            )

        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns SampleDefaults.DEFAULT_PROJECT_ID
        coEvery { upSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectUpSyncScope() {
        runBlockingTest {
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
