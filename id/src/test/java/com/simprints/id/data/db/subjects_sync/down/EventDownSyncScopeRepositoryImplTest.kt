package com.simprints.id.data.db.subjects_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_MODULES
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.sampledata.SampleDefaults.GUID1
import com.simprints.id.sampledata.SampleDefaults.TIME1
import com.simprints.id.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.id.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.id.sampledata.SampleDefaults.userDownSyncScope
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.eventsystem.events_sync.down.domain.getUniqueKey
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventDownSyncScopeRepositoryImplTest {

    companion object {
        private val LAST_EVENT_ID = GUID1
        private val LAST_SYNC_TIME = TIME1
        private val LAST_STATE = DownSyncState.COMPLETE
    }

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncOperationOperationDao: com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao

    private lateinit var eventDownSyncScopeRepository: com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncScopeRepository =
            com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepositoryImpl(
                loginInfoManager,
                preferencesManager,
                downSyncOperationOperationDao
            )

        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
        every { preferencesManager.modalities } returns listOf(Modality.FINGER)
        coEvery { downSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectDownSyncScope() {
        runBlockingTest {
            mockGlobalSyncGroup()

            val syncScope = eventDownSyncScopeRepository.getDownSyncScope()

            assertProjectSyncScope(syncScope)
        }
    }

    @Test
    fun buildUserDownSyncScope() {
        runBlockingTest {
            mockUserSyncGroup()

            val syncScope = eventDownSyncScopeRepository.getDownSyncScope()

            assertUserSyncScope(syncScope)
        }
    }

    @Test
    fun buildModuleDownSyncScope() {
        runBlockingTest {
            mockModuleSyncGroup()

            val syncScope = eventDownSyncScopeRepository.getDownSyncScope()

            assertModuleSyncScope(syncScope)
        }
    }


    @Test
    fun throwWhenProjectIsMissing() {
        runBlockingTest {
            mockGlobalSyncGroup()
            every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope()
            }
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        runBlockingTest {
            mockGlobalSyncGroup()
            every { loginInfoManager.getSignedInUserIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope()
            }
        }
    }

    @Test
    fun downSyncOp_refresh_shouldReturnARefreshedOp() {
        runBlockingTest {

            val refreshedSyncOp = eventDownSyncScopeRepository.refreshState(projectDownSyncScope.operations.first())

            assertThat(refreshedSyncOp).isNotNull()
            refreshedSyncOp.assertProjectSyncOpIsRefreshed()
        }
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() {
        runBlocking {
            eventDownSyncScopeRepository.insertOrUpdate(projectDownSyncScope.operations.first())

            coVerify { downSyncOperationOperationDao.insertOrUpdate(any()) }
        }
    }

    @Test
    fun deleteAll_shouldDeleteOpsFromDb() {
        runBlocking {

            eventDownSyncScopeRepository.deleteAll()

            coVerify { downSyncOperationOperationDao.deleteAll() }
        }
    }

    private fun getSyncOperationsWithLastResult() =
        projectDownSyncScope.operations.map {
            com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState(
                it.getUniqueKey(),
                LAST_STATE,
                LAST_EVENT_ID,
                LAST_SYNC_TIME
            )
        } +
            userDownSyncScope.operations.map {
                com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState(
                    it.getUniqueKey(),
                    LAST_STATE,
                    LAST_EVENT_ID,
                    LAST_SYNC_TIME
                )
            } +
            modulesDownSyncScope.operations.map {
                com.simprints.eventsystem.events_sync.down.local.DbEventsDownSyncOperationState(
                    it.getUniqueKey(),
                    LAST_STATE,
                    LAST_EVENT_ID,
                    LAST_SYNC_TIME
                )
            }


    private fun mockGlobalSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.GLOBAL
    }

    private fun mockUserSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.USER
    }

    private fun mockModuleSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.MODULE
        every { preferencesManager.selectedModules } returns DEFAULT_MODULES.toSet()
    }

    private fun assertProjectSyncScope(syncScope: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectProjectScope::class.java)
        with((syncScope as SubjectProjectScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectUserScope::class.java)
        with((syncScope as SubjectUserScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectModuleScope::class.java)
        with((syncScope as SubjectModuleScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleIds).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.projectId).isNotNull()
        assertThat(queryEvent.moduleIds).isNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }
}
