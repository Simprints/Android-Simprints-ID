package com.simprints.id.data.db.subjects_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.modality.Modes
import com.simprints.core.login.LoginInfoManager
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepositoryImpl
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope.*
import com.simprints.eventsystem.events_sync.down.domain.getUniqueKey
import com.simprints.eventsystem.events_sync.down.local.DbEventDownSyncOperationStateDao
import com.simprints.eventsystem.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULES
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.eventsystem.sampledata.SampleDefaults.TIME1
import com.simprints.eventsystem.sampledata.SampleDefaults.modulesDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.projectDownSyncScope
import com.simprints.eventsystem.sampledata.SampleDefaults.userDownSyncScope
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class EventDownSyncScopeRepositoryImplTest {

    companion object {
        private val LAST_EVENT_ID = GUID1
        private val LAST_SYNC_TIME = TIME1
        private val LAST_STATE = DownSyncState.COMPLETE
    }

    @MockK
    lateinit var loginInfoManager: LoginInfoManager
    @MockK
    lateinit var preferencesManager: IdPreferencesManager
    @MockK
    lateinit var downSyncOperationOperationDao: DbEventDownSyncOperationStateDao

    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncScopeRepository =
            EventDownSyncScopeRepositoryImpl(
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
            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.GLOBAL
            )

            assertProjectSyncScope(syncScope)
        }
    }

    @Test
    fun buildUserDownSyncScope() {
        runBlockingTest {

            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.USER
            )

            assertUserSyncScope(syncScope)
        }
    }

    @Test
    fun buildModuleDownSyncScope() {
        runBlockingTest {
            val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                GROUP.MODULE
            )

            assertModuleSyncScope(syncScope)
        }
    }


    @Test
    fun throwWhenProjectIsMissing() {
        runBlockingTest {
            every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope(
                    listOf(Modes.FINGERPRINT),
                    DEFAULT_MODULES.toList(),
                    GROUP.GLOBAL
                )
            }
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        runBlockingTest {
            every { loginInfoManager.getSignedInUserIdOrEmpty() } returns ""

            assertThrows<MissingArgumentForDownSyncScopeException> {
                eventDownSyncScopeRepository.getDownSyncScope(
                    listOf(Modes.FINGERPRINT),
                    DEFAULT_MODULES.toList(),
                    GROUP.GLOBAL
                )
            }
        }
    }

    @Test
    fun downSyncOp_refresh_shouldReturnARefreshedOp() {
        runBlockingTest {

            val refreshedSyncOp =
                eventDownSyncScopeRepository.refreshState(projectDownSyncScope.operations.first())

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
