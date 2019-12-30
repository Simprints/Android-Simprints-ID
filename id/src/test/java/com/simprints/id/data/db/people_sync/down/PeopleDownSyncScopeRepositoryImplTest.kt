package com.simprints.id.data.db.people_sync.down

import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID_2
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.people_sync.down.domain.ModuleSyncScope
import com.simprints.id.data.db.people_sync.down.domain.ProjectSyncScope
import com.simprints.id.data.db.people_sync.down.domain.UserSyncScope
import com.simprints.id.data.db.people_sync.down.local.DbPeopleDownSyncOperationDao
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.domain.modality.Modes
import com.simprints.id.exceptions.unexpected.MissingArgumentForDownSyncScopeException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class PeopleDownSyncScopeRepositoryImplTest {

    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK(relaxed = true) lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var downSyncOperationOperationDaoDb: DbPeopleDownSyncOperationDao
    lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        peopleDownSyncScopeRepository = PeopleDownSyncScopeRepositoryImpl(loginInfoManager, preferencesManager, downSyncOperationOperationDaoDb)
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns DEFAULT_USER_ID
        every { preferencesManager.modalities } returns listOf(Modality.FINGER)
    }

    @Test
    fun buildProjectDownSyncScope() {
        every { preferencesManager.syncGroup } returns GROUP.GLOBAL

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertThat(syncScope).isInstanceOf(ProjectSyncScope::class.java)
        with((syncScope as ProjectSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }

    @Test
    fun buildUserDownSyncScope() {
        every { preferencesManager.syncGroup } returns GROUP.USER

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertThat(syncScope).isInstanceOf(UserSyncScope::class.java)
        with((syncScope as UserSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }


    @Test
    fun buildModuleDownSyncScope() {
        every { preferencesManager.syncGroup } returns GROUP.MODULE
        val selectedModules = setOf(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
        every { preferencesManager.selectedModules } returns selectedModules

        val syncScope = peopleDownSyncScopeRepository.getDownSyncScope()

        assertThat(syncScope).isInstanceOf(ModuleSyncScope::class.java)
        with((syncScope as ModuleSyncScope)) {
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(modules).containsExactly(DEFAULT_MODULE_ID, DEFAULT_MODULE_ID_2)
            assertThat(modes).isEqualTo(listOf(Modes.FINGERPRINT))
        }
    }


    @Test
    fun throwWhenProjectIsMissing() {
        every { preferencesManager.syncGroup } returns GROUP.GLOBAL
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            peopleDownSyncScopeRepository.getDownSyncScope()
        }
    }

    @Test
    fun throwWhenUserIsMissing() {
        every { preferencesManager.syncGroup } returns GROUP.USER
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns ""

        assertThrows<MissingArgumentForDownSyncScopeException> {
            peopleDownSyncScopeRepository.getDownSyncScope()
        }
    }
}
