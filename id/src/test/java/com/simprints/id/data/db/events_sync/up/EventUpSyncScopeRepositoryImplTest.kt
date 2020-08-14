package com.simprints.id.data.db.events_sync.up

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants
import com.simprints.id.commontesttools.DefaultTestConstants.TIME1
import com.simprints.id.commontesttools.DefaultTestConstants.projectUpSyncScope
import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation.UpSyncState.COMPLETE
import com.simprints.id.data.db.events_sync.up.domain.getUniqueKey
import com.simprints.id.data.db.events_sync.up.local.DbEventUpSyncOperationStateDao
import com.simprints.id.data.db.events_sync.up.local.DbEventsUpSyncOperationState
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
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
    @MockK lateinit var upSyncOperationOperationDao: DbEventUpSyncOperationStateDao

    private lateinit var eventUpSyncScopeRepository: EventUpSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventUpSyncScopeRepository = EventUpSyncScopeRepositoryImpl(loginInfoManager, upSyncOperationOperationDao)

        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DefaultTestConstants.DEFAULT_PROJECT_ID
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

    private fun getSyncOperationsWithLastResult(): List<DbEventsUpSyncOperationState> {
        val op = projectUpSyncScope.operation
        return listOf(DbEventsUpSyncOperationState(op.getUniqueKey(), COMPLETE, TIME1))
    }
}
