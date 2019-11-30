package com.simprints.id.data.db.project.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.RealmTestsBase
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.toRealmProject
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.LocalDbKey
import com.simprints.id.data.secure.LegacyLocalDbKeyProvider
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.realm.Realm
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectLocalDataSourceTest : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var projectLocalDataSource: ProjectLocalDataSource
    private val project = Project().apply {
        id = DEFAULT_PROJECT_ID
        name = "some_name"
        description = "some_description"
        creator = "some_creator"
    }


    private val loginInfoManagerMock = mock<LoginInfoManager>().apply {
        whenever(this) { getSignedInProjectIdOrEmpty() }
            .thenReturn(DEFAULT_PROJECT_ID)
    }
    private val secureDataManagerMock = mock<LegacyLocalDbKeyProvider>().apply {
        whenever(this) { getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID) }
            .thenReturn(LocalDbKey(newDatabaseName, newDatabaseKey))
    }

    @Before
    fun setup() {
        realm = Realm.getInstance(config)
        realm.executeTransaction {
            it.where(DbProject::class.java).findAll().deleteAllFromRealm()
        }
        projectLocalDataSource = ProjectLocalDataSourceImpl(testContext, secureDataManagerMock, loginInfoManagerMock)
    }

    @Test
    fun save_shouldStoreAProjectInTheDb() = runBlocking {
        projectLocalDataSource.save(project)

        realm.executeTransaction {
            assertThat(realm.where(DbProject::class.java).findFirst()?.id).isEqualTo(DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun load_shouldLoadAProjectFromTheDb() = runBlocking {
        realm.executeTransaction {
            assertThat(it.insertOrUpdate(project.toRealmProject()))
        }

        val projectFromDb = projectLocalDataSource.load(DEFAULT_PROJECT_ID)

        assertThat(projectFromDb?.id).isEqualTo(project.id)
    }

    @Test
    fun loadFails_shouldReturnANullProject() = runBlocking {
        val projectFromDb = projectLocalDataSource.load(DEFAULT_PROJECT_ID)

        assertThat(projectFromDb).isNull()
    }
}
