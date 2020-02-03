package com.simprints.id.data.db.people_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.data.db.people_sync.down.domain.*
import com.simprints.id.data.db.people_sync.down.domain.PeopleDownSyncOperationResult.DownSyncState.COMPLETE
import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncOperationLocalDataSource
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

class PeopleDownSyncScopeRepositoryImplTest {

    companion object {
        const val LAST_PATIENT_ID = "lastPatientId"
        const val LAST_PATIENT_UPDATED_AT = 1L
        const val LAST_SYNC_TIME = 2L
    }

    private val selectedModules = setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
    private val projectSyncOp = PeopleDownSyncOperation(DEFAULT_PROJECT_ID, null, null, DEFAULT_MODES, null)
    private val userSyncOp = PeopleDownSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null, DEFAULT_MODES, null)
    private val moduleSyncOp = PeopleDownSyncOperation(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID, DEFAULT_MODES, null)
    private val lastDownSyncResult = PeopleDownSyncOperationResult(COMPLETE, LAST_PATIENT_ID, LAST_PATIENT_UPDATED_AT, LAST_SYNC_TIME)

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncOperationOperationDao: PeopleDownSyncOperationLocalDataSource
    @MockK lateinit var peopleDownSyncOperationFactory: PeopleDownSyncOperationFactory

    lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        peopleDownSyncScopeRepository = spyk(PeopleDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, downSyncOperationOperationDao, peopleDownSyncOperationFactory))
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
        every { preferencesManager.modalities } returns listOf(Modality.FINGER)
        coEvery { downSyncOperationOperationDao.getDownSyncOperationsAll() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectDownSyncScope() {
        mockGlobalSyncGroup()

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertProjectSyncScope(syncScope)
    }

    @Test
    fun buildUserDownSyncScope() {
        mockUserSyncGroup()

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertUserSyncScope(syncScope)
    }

    @Test
    fun buildModuleDownSyncScope() {
        mockModuleSyncGroup()

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertModuleSyncScope(syncScope)
    }


    @Test
    fun throwWhenProjectIsMissing() {
        mockGlobalSyncGroup()
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            peopleDownSyncScopeRepository.getDownSyncScope()
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        mockGlobalSyncGroup()
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            peopleDownSyncScopeRepository.getDownSyncScope()
        }
    }

    @Test
    fun givenProjectSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { peopleDownSyncOperationFactory.buildProjectSyncOperation(any(), any(), any()) } returns projectSyncOp
        coEvery { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns projectSyncOp

        peopleDownSyncScopeRepository.getDownSyncOperations(projectSyncScope)

        with(projectSyncScope) {
            verify { peopleDownSyncOperationFactory.buildProjectSyncOperation(projectId, modes, null) }
            coVerify { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(projectSyncOp) }
        }
    }

    @Test
    fun givenUserSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { peopleDownSyncOperationFactory.buildUserSyncOperation(any(), any(), any(), any()) } returns userSyncOp
        coEvery { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns userSyncOp

        peopleDownSyncScopeRepository.getDownSyncOperations(userSyncScope)

        with(userSyncScope) {
            verify { peopleDownSyncOperationFactory.buildUserSyncOperation(projectId, userId, modes, null) }
            coVerify { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(userSyncOp) }
        }
    }

    @Test
    fun givenModuleSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { peopleDownSyncOperationFactory.buildModuleSyncOperation(any(), any(), any(), any()) } returns moduleSyncOp
        coEvery { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns moduleSyncOp

        peopleDownSyncScopeRepository.getDownSyncOperations(moduleSyncScope)

        with(moduleSyncScope) {
            verify { peopleDownSyncOperationFactory.buildModuleSyncOperation(projectId, modules[0], modes, null) }
            verify { peopleDownSyncOperationFactory.buildModuleSyncOperation(projectId, modules[1], modes, null) }
            coVerify { peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(moduleSyncOp) }
        }
    }


    @Test
    fun givenProjectSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(projectSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertProjectSyncOpIsRefreshed()
    }

    @Test
    fun givenUserSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(userSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertUserSyncOpIsRefreshed()
    }

    @Test
    fun givenModuleSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = peopleDownSyncScopeRepository.refreshDownSyncOperationFromDb(moduleSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertModuleSyncOpIsRefreshed()
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() = runBlockingTest {

        peopleDownSyncScopeRepository.insertOrUpdate(projectSyncOp)

        coVerify { downSyncOperationOperationDao.insertOrReplaceDownSyncOperation(any()) }
    }

    @Test
    fun deleteAll_shouldDeleteOpsFromDb() = runBlockingTest {

        peopleDownSyncScopeRepository.deleteAll()

        coVerify { downSyncOperationOperationDao.deleteAll() }
    }

    private fun getSyncOperationsWithLastResult() =
        listOf(
            projectSyncOp.copy(lastResult = lastDownSyncResult).fromDomainToDb(),
            userSyncOp.copy(lastResult = lastDownSyncResult).fromDomainToDb(),
            moduleSyncOp.copy(lastResult = lastDownSyncResult).fromDomainToDb())

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

    private fun assertProjectSyncScope(syncScope: PeopleDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ProjectSyncScope::class.java)
        with((syncScope as ProjectSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: PeopleDownSyncScope) {
        assertThat(syncScope).isInstanceOf(UserSyncScope::class.java)
        with((syncScope as UserSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: PeopleDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ModuleSyncScope::class.java)
        with((syncScope as ModuleSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modules).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun PeopleDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(projectSyncOp.projectId)
        assertThat(userId).isNull()
        assertThat(moduleId).isNull()
        assertThat(modes).isEqualTo(projectSyncOp.modes)
    }

    private fun PeopleDownSyncOperation.assertUserSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(userSyncOp.projectId)
        assertThat(userId).isEqualTo(userSyncOp.userId)
        assertThat(moduleId).isNull()
        assertThat(modes).isEqualTo(userSyncOp.modes)
    }

    private fun PeopleDownSyncOperation.assertModuleSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(moduleSyncOp.projectId)
        assertThat(moduleId).isEqualTo(moduleSyncOp.moduleId)
        assertThat(userId).isNull()
        assertThat(modes).isEqualTo(moduleSyncOp.modes)
    }
}
