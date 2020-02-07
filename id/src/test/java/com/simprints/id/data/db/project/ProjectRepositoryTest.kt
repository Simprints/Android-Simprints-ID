package com.simprints.id.data.db.project

import android.accounts.NetworkErrorException
import com.google.common.truth.Truth.assertThat
import com.google.firebase.perf.FirebasePerformance
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/*
 * runBlockingTest will make these tests fail by throwing an IllegalStateException:
 * This job has not completed yet. Replacing it with runBlocking has fixed the issue.
 * Source: https://github.com/Kotlin/kotlinx.coroutines/issues/1204
 */
@ExperimentalCoroutinesApi
class ProjectRepositoryTest {

    private val localProject = Project(DEFAULT_PROJECT_ID, "local", "",  "")
    private val remoteProject = Project(DEFAULT_PROJECT_ID, "remote", "",  "")

    private val projectRemoteDataSourceMock: ProjectRemoteDataSource = mockk()
    private val projectLocalDataSourceMock: ProjectLocalDataSource = mockk(relaxUnitFun = true)
    private val firebasePerformanceMock: FirebasePerformance = mockk()

    private val projectRepository = ProjectRepositoryImpl(projectLocalDataSourceMock, projectRemoteDataSourceMock, firebasePerformanceMock)

    @Before
    fun setup() {
        BaseUnitTestConfig().coroutinesMainThread()

        every { firebasePerformanceMock.newTrace(any()) } returns mockk(relaxUnitFun = true)
    }

    @Test
    fun givenAProjectStoredLocally_shouldBeLoadedAndCacheRefreshed() = runBlocking {
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns localProject
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns remoteProject

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(localProject)
        coVerify { projectLocalDataSourceMock.save(remoteProject) }
        coVerify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenAProjectStoredLocallyAndNotRemotely_shouldBeLoadedAndCacheNoRefreshed() = runBlocking {
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns localProject
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } throws NetworkErrorException("")

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(localProject)
        coVerify(exactly = 0) { projectLocalDataSourceMock.save(remoteProject) }
        coVerify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenProjectStoredOnlyRemotely_shouldBeFetchedAndCacheRefreshed() = runBlocking {
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns null
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns remoteProject

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(remoteProject)
        coVerify { projectLocalDataSourceMock.save(remoteProject) }
        coVerify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenNoProjectStored_noErrorShouldBeThrown() = runBlocking {
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns null
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } throws NetworkErrorException("")

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isNull()
        coVerify(exactly = 0) { projectLocalDataSourceMock.save(remoteProject) }
        coVerify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

}
