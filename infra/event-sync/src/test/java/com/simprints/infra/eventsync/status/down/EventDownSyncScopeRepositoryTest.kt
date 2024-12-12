package com.simprints.infra.eventsync.status.down

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.Partitioning
import com.simprints.core.domain.modality.Modes
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULES
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_MODULE_ID_2
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.events.sampledata.SampleDefaults.GUID1
import com.simprints.infra.events.sampledata.SampleDefaults.TIME1
import com.simprints.infra.eventsync.SampleSyncScopes.modulesDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.projectDownSyncScope
import com.simprints.infra.eventsync.SampleSyncScopes.userDownSyncScope
import com.simprints.infra.eventsync.exceptions.MissingArgumentForDownSyncScopeException
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectModuleScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectProjectScope
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope.SubjectUserScope
import com.simprints.infra.eventsync.status.down.local.DbEventDownSyncOperationStateDao
import com.simprints.infra.eventsync.status.down.local.DbEventsDownSyncOperationState
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class EventDownSyncScopeRepositoryTest {
    companion object {
        private val LAST_EVENT_ID = GUID1
        private val LAST_SYNC_TIME = TIME1
        private val LAST_STATE = DownSyncState.COMPLETE
    }

    @MockK
    lateinit var authStore: AuthStore

    @MockK
    lateinit var recentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var downSyncOperationOperationDao: DbEventDownSyncOperationStateDao

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var tokenizationProcessor: TokenizationProcessor

    private lateinit var eventDownSyncScopeRepository: EventDownSyncScopeRepository

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        eventDownSyncScopeRepository =
            EventDownSyncScopeRepository(
                authStore,
                recentUserActivityManager,
                downSyncOperationOperationDao,
                configManager,
                tokenizationProcessor,
            )

        every { authStore.signedInProjectId } returns DEFAULT_PROJECT_ID
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns mockk {
            every { lastUserUsed } returns DEFAULT_USER_ID
        }
        coEvery { downSyncOperationOperationDao.load() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectDownSyncScope() = runTest(UnconfinedTestDispatcher()) {
        val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
            listOf(Modes.FINGERPRINT),
            DEFAULT_MODULES.toList(),
            Partitioning.GLOBAL,
        )

        assertProjectSyncScope(syncScope)
    }

    @Test
    fun buildUserDownSyncScope() = runTest(UnconfinedTestDispatcher()) {
        every { authStore.signedInUserId } returns DEFAULT_USER_ID
        every { tokenizationProcessor.encrypt(any(), any(), any()) } returns TokenizableString.Tokenized(DEFAULT_USER_ID.value)

        val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
            listOf(Modes.FINGERPRINT),
            DEFAULT_MODULES.toList(),
            Partitioning.USER,
        )

        assertUserSyncScope(syncScope)
    }

    @Test
    fun buildUserDownSyncScopeWhenNoSaved() = runTest(UnconfinedTestDispatcher()) {
        every { authStore.signedInUserId } returns null
        every { tokenizationProcessor.encrypt(any(), any(), any()) } returns TokenizableString.Tokenized(DEFAULT_USER_ID.value)

        val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
            listOf(Modes.FINGERPRINT),
            DEFAULT_MODULES.toList(),
            Partitioning.USER,
        )

        assertUserSyncScope(syncScope)
    }

    @Test
    fun buildUserDownSyncScopeWhenUserTokenised() = runTest(UnconfinedTestDispatcher()) {
        every { authStore.signedInUserId } returns TokenizableString.Tokenized(DEFAULT_USER_ID.value)

        val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
            listOf(Modes.FINGERPRINT),
            DEFAULT_MODULES.toList(),
            Partitioning.USER,
        )

        assertUserSyncScope(syncScope)
    }

    @Test
    fun buildModuleDownSyncScope() = runTest(UnconfinedTestDispatcher()) {
        val syncScope = eventDownSyncScopeRepository.getDownSyncScope(
            listOf(Modes.FINGERPRINT),
            DEFAULT_MODULES.toList(),
            Partitioning.MODULE,
        )

        assertModuleSyncScope(syncScope)
    }

    @Test
    fun throwWhenProjectIsMissing() = runTest(UnconfinedTestDispatcher()) {
        every { authStore.signedInProjectId } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                Partitioning.GLOBAL,
            )
        }
    }

    @Test
    fun throwWhenUserIsMissing() = runTest(UnconfinedTestDispatcher()) {
        coEvery { authStore.signedInUserId } returns null
        coEvery { recentUserActivityManager.getRecentUserActivity() } returns mockk {
            every { lastUserUsed } returns "".asTokenizableEncrypted()
        }

        assertThrows<MissingArgumentForDownSyncScopeException> {
            eventDownSyncScopeRepository.getDownSyncScope(
                listOf(Modes.FINGERPRINT),
                DEFAULT_MODULES.toList(),
                Partitioning.USER,
            )
        }
    }

    @Test
    fun downSyncOp_refresh_shouldReturnARefreshedOp() = runTest(UnconfinedTestDispatcher()) {
        val refreshedSyncOp =
            eventDownSyncScopeRepository.refreshState(projectDownSyncScope.operations.first())

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp.assertProjectSyncOpIsRefreshed()
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() = runTest {
        eventDownSyncScopeRepository.insertOrUpdate(projectDownSyncScope.operations.first())

        coVerify { downSyncOperationOperationDao.insertOrUpdate(any()) }
    }

    @Test
    fun deleteOperations_shouldDeleteOpsFromDb() = runTest {
        eventDownSyncScopeRepository.deleteOperations(
            DEFAULT_MODULES.toList(),
            listOf(Modes.FINGERPRINT),
        )

        DEFAULT_MODULES.forEach { moduleId ->
            val scope = SubjectModuleScope(
                DEFAULT_PROJECT_ID,
                listOf(moduleId),
                listOf(Modes.FINGERPRINT),
            )
            coVerify(exactly = 1) {
                downSyncOperationOperationDao.delete(
                    scope.operations.first().getUniqueKey(),
                )
            }
        }
    }

    @Test
    fun deleteAll_shouldDeleteAllOpsFromDb() = runTest {
        eventDownSyncScopeRepository.deleteAll()

        coVerify { downSyncOperationOperationDao.deleteAll() }
    }

    private fun getSyncOperationsWithLastResult() = projectDownSyncScope.operations.map {
        DbEventsDownSyncOperationState(
            it.getUniqueKey(),
            LAST_STATE,
            LAST_EVENT_ID,
            LAST_SYNC_TIME,
        )
    } +
        userDownSyncScope.operations.map {
            DbEventsDownSyncOperationState(
                it.getUniqueKey(),
                LAST_STATE,
                LAST_EVENT_ID,
                LAST_SYNC_TIME,
            )
        } +
        modulesDownSyncScope.operations.map {
            DbEventsDownSyncOperationState(
                it.getUniqueKey(),
                LAST_STATE,
                LAST_EVENT_ID,
                LAST_SYNC_TIME,
            )
        }

    private fun assertProjectSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectProjectScope::class.java)
        with((syncScope as SubjectProjectScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectUserScope::class.java)
        with((syncScope as SubjectUserScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID.value)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: EventDownSyncScope) {
        assertThat(syncScope).isInstanceOf(SubjectModuleScope::class.java)
        with((syncScope as SubjectModuleScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(moduleIds).containsExactly(
                DEFAULT_MODULE_ID.value,
                DEFAULT_MODULE_ID_2.value,
            )
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun EventDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastEventId).isEqualTo(LAST_EVENT_ID)
        assertThat(lastSyncTime).isEqualTo(LAST_SYNC_TIME)
        assertThat(state).isEqualTo(LAST_STATE)
        assertThat(queryEvent.projectId).isNotNull()
        assertThat(queryEvent.moduleId).isNull()
        assertThat(queryEvent.modes).isEqualTo(DEFAULT_MODES)
    }
}
