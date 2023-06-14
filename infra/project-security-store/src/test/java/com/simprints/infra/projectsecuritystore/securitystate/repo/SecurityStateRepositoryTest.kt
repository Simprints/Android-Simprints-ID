package com.simprints.infra.projectsecuritystore.securitystate.repo

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import com.simprints.infra.projectsecuritystore.SecurityStateRepository
import com.simprints.infra.projectsecuritystore.securitystate.models.SecurityState
import com.simprints.infra.projectsecuritystore.securitystate.repo.local.SecurityStateLocalDataSource
import com.simprints.infra.projectsecuritystore.securitystate.repo.remote.SecurityStateRemoteDataSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class SecurityStateRepositoryTest {

    @MockK
    lateinit var mockRemoteDataSource: SecurityStateRemoteDataSource

    @MockK
    lateinit var mockLocalDataSource: SecurityStateLocalDataSource

    private lateinit var repository: SecurityStateRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(Dispatchers.Unconfined)

        repository = SecurityStateRepositoryImpl(mockRemoteDataSource, mockLocalDataSource)
    }

    @Test
    fun shouldGetSecurityStateFromRemote() = runTest {
        val expected = SecurityState(DEVICE_ID, SecurityState.Status.PROJECT_ENDED)
        coEvery { mockRemoteDataSource.getSecurityState() } returns expected

        val securityState = repository.getSecurityStatusFromRemote()

        assertThat(securityState).isEqualTo(expected)
    }

    @Test
    fun getSecurityState_shouldUpdateLocalDataSource() = runTest {
        val status = SecurityState.Status.RUNNING
        val state = SecurityState(DEVICE_ID, status)
        coEvery { mockRemoteDataSource.getSecurityState() } returns state

        repository.getSecurityStatusFromRemote()

        verify { mockLocalDataSource.securityStatus = status }
    }

    @Test(expected = SyncCloudIntegrationException::class)
    fun remoteDataSourceThrowsSyncCloudIntegrationException_repositoryShouldThrow() = runTest {
        coEvery {
            mockRemoteDataSource.getSecurityState()
        } throws SyncCloudIntegrationException(cause = Exception())

        repository.getSecurityStatusFromRemote()
    }

    @Test(expected = HttpException::class)
    fun remoteDataSourceThrowsHttpException_repositoryShouldThrow() = runTest {
        coEvery { mockRemoteDataSource.getSecurityState() } throws HttpException(
            Response.error<Any>(500, "".toResponseBody("".toMediaTypeOrNull()))
        )

        repository.getSecurityStatusFromRemote()
    }

    @Test
    fun shouldSendSecurityStatusThroughChannel() = runTest {
        val securityState = SecurityState(DEVICE_ID, SecurityState.Status.PROJECT_ENDED)
        coEvery { mockRemoteDataSource.getSecurityState() } returns securityState

        val remoteState = repository.getSecurityStatusFromRemote()

        assertThat(securityState).isEqualTo(
            remoteState
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private companion object {
        const val DEVICE_ID = "mock-device-id"
    }

}
