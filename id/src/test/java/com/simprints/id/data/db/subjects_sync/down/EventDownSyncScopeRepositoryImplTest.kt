package com.simprints.id.data.db.subjects_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.modulesSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.id.data.db.events_sync.down.domain.getUniqueKey
import com.simprints.id.data.db.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.id.data.db.events_sync.down.local.DbEventsDownSyncOperationState
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class EventDownSyncScopeRepositoryImplTest {

    companion object {
        private const val LAST_EVENT_ID = "lastPatientId"
        private const val LAST_SYNC_TIME = 2L
        private val LAST_STATE = DownSyncState.COMPLETE
    }

    private val selectedModules = setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
    private val projectDownSyncOp = projectSyncScope.operations.first()
    private val userDownSyncOp = userSyncScope.operations.first()
    private val moduleDownSyncOp = modulesSyncScope.operations.first()

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncOperationOperationDao: DbEventDownSyncOperationStateDao

    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncScopeRepository = spyk(EventDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, downSyncOperationOperationDao))

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
    fun givenUserSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = eventDownSyncScopeRepository.refreshState(userDownSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp.assertUserSyncOpIsRefreshed()
    }

    @Test
    fun givenModuleSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = eventDownSyncScopeRepository.refreshState(moduleDownSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp.assertModuleSyncOpIsRefreshed()
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() = runBlockingTest {

        eventDownSyncScopeRepository.insertOrUpdate(projectSyncScope.operations.first())

        coVerify { downSyncOperationOperationDao.insertOrUpdate(any()) }
    }

    @Test
    fun deleteAll_shouldDeleteOpsFromDb() = runBlockingTest {

        eventDownSyncScopeRepository.deleteAll()

        coVerify { downSyncOperationOperationDao.deleteAll() }
    }

    private fun getSyncOperationsWithLastResult() =
        projectSyncScope.operations.map { DbEventsDownSyncOperationState(it.getUniqueKey(), LAST_STATE, LAST_EVENT_ID, LAST_SYNC_TIME) } +
            userSyncScope.operations.map { DbEventsDownSyncOperationState(it.getUniqueKey(), LAST_STATE, LAST_EVENT_ID, LAST_SYNC_TIME) } +
            modulesSyncScope.operations.map { DbEventsDownSyncOperationState(it.getUniqueKey(), LAST_STATE, LAST_EVENT_ID, LAST_SYNC_TIME) }


    private fun mockGlobalSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.GLOBAL
    }

    private fun mockUserSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.USER
    }

    private fun mockModuleSyncGroup() {
        every { preferencesManager.syncGroup } returns GROUP.MODULE
        every { preferencesManager.selectedModules } returns selectedModules
    }

    private fun assertProjectSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ProjectScope::class.java)
        with((syncScope as ProjectScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(UserScope::class.java)
        with((syncScope as UserScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ModuleScope::class.java)
        with((syncScope as ModuleScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleIds).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun EventDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.projectId).isNull()
        assertThat(queryEvent.moduleIds).isNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }

    private fun EventDownSyncOperation.assertUserSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.attendantId).isEqualTo(DEFAULT_USER_ID)
        assertThat(queryEvent.projectId).isNull()
        assertThat(queryEvent.moduleIds).isNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }

    private fun EventDownSyncOperation.assertModuleSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.projectId).isNull()
        assertThat(queryEvent.moduleIds).isEqualTo(listOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2))
        assertThat(queryEvent.attendantId).isNotNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }
}
