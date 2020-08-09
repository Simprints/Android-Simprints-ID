package com.simprints.id.data.db.subjects_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODES
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.DefaultTestConstants.moduleSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.projectSyncScope
import com.simprints.id.commontesttools.DefaultTestConstants.userSyncScope
import com.simprints.id.data.db.subjects_sync.down.domain.*
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
        const val LAST_PATIENT_ID = "lastPatientId"
        const val LAST_PATIENT_UPDATED_AT = 1L
        const val LAST_SYNC_TIME = 2L
    }

    private val selectedModules = setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
    private val projectSyncOp = EventsDownSyncOperation(DEFAULT_PROJECT_ID, null, null, DEFAULT_MODES, null)
    private val userSyncOp = EventsDownSyncOperation(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, null, DEFAULT_MODES, null)
    private val moduleSyncOp = EventsDownSyncOperation(DEFAULT_PROJECT_ID, null, DEFAULT_MODULE_ID, DEFAULT_MODES, null)
    private val lastDownSyncResult = EventsDownSyncOperationResult(COMPLETE, LAST_PATIENT_ID, LAST_PATIENT_UPDATED_AT)

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncOperationOperationDao: EventDownSyncOperationLocalDataSource
    @MockK lateinit var EventsDownSyncOperationFactory: EventsDownSyncOperationFactory

    lateinit var subjectsDownSyncScopeRepository: SubjectsDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        subjectsDownSyncScopeRepository = spyk(SubjectsDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, downSyncOperationOperationDao, EventsDownSyncOperationFactory))
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
        every { preferencesManager.modalities } returns listOf(Modality.FINGER)
        coEvery { downSyncOperationOperationDao.getDownSyncOperationsAll() } returns getSyncOperationsWithLastResult()
    }

    @Test
    fun buildProjectDownSyncScope() {
        mockGlobalSyncGroup()

        val syncScope = subjectsDownSyncScopeRepository.getDownSyncScope()

        assertProjectSyncScope(syncScope)
    }

    @Test
    fun buildUserDownSyncScope() {
        mockUserSyncGroup()

        val syncScope = subjectsDownSyncScopeRepository.getDownSyncScope()

        assertUserSyncScope(syncScope)
    }

    @Test
    fun buildModuleDownSyncScope() {
        mockModuleSyncGroup()

        val syncScope = subjectsDownSyncScopeRepository.getDownSyncScope()

        assertModuleSyncScope(syncScope)
    }


    @Test
    fun throwWhenProjectIsMissing() {
        mockGlobalSyncGroup()
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            subjectsDownSyncScopeRepository.getDownSyncScope()
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        mockGlobalSyncGroup()
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            subjectsDownSyncScopeRepository.getDownSyncScope()
        }
    }

    @Test
    fun givenProjectSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { EventsDownSyncOperationFactory.buildProjectSyncOperation(any(), any(), any()) } returns projectSyncOp
        coEvery { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns projectSyncOp

        subjectsDownSyncScopeRepository.getDownSyncOperations(projectSyncScope)

        with(projectSyncScope) {
            verify { EventsDownSyncOperationFactory.buildProjectSyncOperation(projectId, modes, null) }
            coVerify { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(projectSyncOp) }
        }
    }

    @Test
    fun givenUserSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { EventsDownSyncOperationFactory.buildUserSyncOperation(any(), any(), any(), any()) } returns userSyncOp
        coEvery { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns userSyncOp

        subjectsDownSyncScopeRepository.getDownSyncOperations(userSyncScope)

        with(userSyncScope) {
            verify { EventsDownSyncOperationFactory.buildUserSyncOperation(projectId, userId, modes, null) }
            coVerify { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(userSyncOp) }
        }
    }

    @Test
    fun givenModuleSyncGroup_getDownSyncOperations_shouldReturnSyncOps() = runBlockingTest {
        every { EventsDownSyncOperationFactory.buildModuleSyncOperation(any(), any(), any(), any()) } returns moduleSyncOp
        coEvery { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(any()) } returns moduleSyncOp

        subjectsDownSyncScopeRepository.getDownSyncOperations(moduleSyncScope)

        with(moduleSyncScope) {
            verify { EventsDownSyncOperationFactory.buildModuleSyncOperation(projectId, modules[0], modes, null) }
            verify { EventsDownSyncOperationFactory.buildModuleSyncOperation(projectId, modules[1], modes, null) }
            coVerify { subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(moduleSyncOp) }
        }
    }


    @Test
    fun givenProjectSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(projectSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertProjectSyncOpIsRefreshed()
    }

    @Test
    fun givenUserSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(userSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertUserSyncOpIsRefreshed()
    }

    @Test
    fun givenModuleSyncGroup_refreshDownSyncOperationFromDb_shouldReturnARefreshedSyncScope() = runBlockingTest {

        val refreshedSyncOp = subjectsDownSyncScopeRepository.refreshDownSyncOperationFromDb(moduleSyncOp)

        assertThat(refreshedSyncOp).isNotNull()
        refreshedSyncOp?.assertModuleSyncOpIsRefreshed()
    }

    @Test
    fun insertOrUpdate_shouldInsertIntoTheDb() = runBlockingTest {

        subjectsDownSyncScopeRepository.insertOrUpdate(projectSyncOp)

        coVerify { downSyncOperationOperationDao.insertOrReplaceDownSyncOperation(any()) }
    }

    @Test
    fun deleteAll_shouldDeleteOpsFromDb() = runBlockingTest {

        subjectsDownSyncScopeRepository.deleteAll()

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

    private fun assertProjectSyncScope(syncScope: SubjectsDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ProjectSyncScope::class.java)
        with((syncScope as ProjectSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertUserSyncScope(syncScope: SubjectsDownSyncScope) {
        assertThat(syncScope).isInstanceOf(UserSyncScope::class.java)
        with((syncScope as UserSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun assertModuleSyncScope(syncScope: SubjectsDownSyncScope) {
        assertThat(syncScope).isInstanceOf(ModuleSyncScope::class.java)
        with((syncScope as ModuleSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modules).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    private fun EventsDownSyncOperation.assertProjectSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(projectSyncOp.projectId)
        assertThat(attendantId).isNull()
        assertThat(moduleId).isNull()
        assertThat(modes).isEqualTo(projectSyncOp.modes)
    }

    private fun EventsDownSyncOperation.assertUserSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(userSyncOp.projectId)
        assertThat(attendantId).isEqualTo(userSyncOp.attendantId)
        assertThat(moduleId).isNull()
        assertThat(modes).isEqualTo(userSyncOp.modes)
    }

    private fun EventsDownSyncOperation.assertModuleSyncOpIsRefreshed() {
        assertThat(lastResult).isEqualTo(lastDownSyncResult)
        assertThat(projectId).isEqualTo(moduleSyncOp.projectId)
        assertThat(moduleId).isEqualTo(moduleSyncOp.moduleId)
        assertThat(attendantId).isNull()
        assertThat(modes).isEqualTo(moduleSyncOp.modes)
    }
}
