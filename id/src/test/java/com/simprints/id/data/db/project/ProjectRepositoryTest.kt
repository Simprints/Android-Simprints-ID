package com.simprints.id.data.db.project

import android.accounts.NetworkErrorException
import com.google.common.truth.Truth.assertThat
import com.google.firebase.perf.FirebasePerformance
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.testtools.unit.BaseUnitTestConfig
import io.mockk.*
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ProjectRepositoryTest {

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

        val localProject = Project()
        val remoteProject = Project()
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns localProject
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns Single.just(remoteProject)

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(localProject)
        coVerify { projectLocalDataSourceMock.save(remoteProject) }
        verify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenAProjectStoredLocallyAndNotRemotely_shouldBeLoadedAndCacheNoRefreshed() = runBlocking {

        val localProject = Project()
        val remoteProject = Project()
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns localProject
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns Single.error(NetworkErrorException(""))

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(localProject)
        coVerify(exactly = 0) { projectLocalDataSourceMock.save(remoteProject) }
        verify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenProjectStoredOnlyRemotely_shouldBeFetchedAndCacheRefreshed() = runBlocking {

        val remoteProject = Project()
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns null
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns Single.just(remoteProject)

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isEqualTo(remoteProject)
        coVerify { projectLocalDataSourceMock.save(remoteProject) }
        verify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun givenNoProjectStored_noErrorShouldBeThrown() = runBlocking {

        val remoteProject = Project()
        coEvery { projectLocalDataSourceMock.load(DEFAULT_PROJECT_ID) } returns null
        coEvery { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) } returns Single.error(NetworkErrorException(""))

        val project = projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID)

        assertThat(project).isNull()
        coVerify(exactly = 0) { projectLocalDataSourceMock.save(remoteProject) }
        verify { projectRemoteDataSourceMock.loadProjectFromRemote(DEFAULT_PROJECT_ID) }
    }

}
