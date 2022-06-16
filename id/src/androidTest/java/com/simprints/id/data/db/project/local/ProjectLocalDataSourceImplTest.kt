package com.simprints.eventsystem.project.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.security.LocalDbKey
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.tools.coroutines.DefaultDispatcherProvider
import com.simprints.eventsystem.RealmTestsBase
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.local.ProjectLocalDataSourceImpl
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.fromDomainToDb
import com.simprints.id.data.db.subject.local.RealmWrapperImpl
import io.mockk.every
import io.mockk.mockk
import io.realm.Realm
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectLocalDataSourceImplTest : RealmTestsBase() {

    private lateinit var realm: Realm
    private lateinit var projectLocalDataSource: ProjectLocalDataSource

    private val loginInfoManagerMock = mockk<LoginInfoManager>().apply {
        every { getSignedInProjectIdOrEmpty() } returns DEFAULT_PROJECT_ID
    }

    private val secureLocalDbKeyProviderMock = mockk<SecureLocalDbKeyProvider>().apply {
        every {
            getLocalDbKeyOrThrow(DEFAULT_PROJECT_ID)
        } returns LocalDbKey(newDatabaseName, newDatabaseKey)

    }
    private val testDispatcherProvider = DefaultDispatcherProvider()

    private val project = Project(
        DEFAULT_PROJECT_ID,
        "some_name",
        "some_description",
        "some_creator",
        "some_image_bucket"
    )


    @Before
    fun setup() {
        realm = Realm.getInstance(config).apply {
            executeTransaction {
                it.where(DbProject::class.java).findAll().deleteAllFromRealm()
            }
        }

        projectLocalDataSource = ProjectLocalDataSourceImpl(
            RealmWrapperImpl(
                testContext,
                secureLocalDbKeyProviderMock,
                loginInfoManagerMock,
                testDispatcherProvider
            )
        )
    }

    @Test
    fun save_shouldStoreAProjectInTheDb() = runBlocking {
        projectLocalDataSource.save(project)

        realm.executeTransaction {
            assertThat(realm.where(DbProject::class.java).findFirst()?.id)
                .isEqualTo(DEFAULT_PROJECT_ID)
        }
    }

    @Test
    fun load_shouldLoadAProjectFromTheDb() = runBlocking {
        realm.executeTransaction {
            assertThat(it.insertOrUpdate(project.fromDomainToDb()))
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
