package com.simprints.id.secure.securitystate.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import com.simprints.infra.network.exceptions.SyncCloudIntegrationException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class SecurityStateRepositoryImplTest {

    @MockK
    lateinit var mockRemoteDataSource: SecurityStateRemoteDataSource
    @MockK
    lateinit var mockLocalDataSource: SecurityStateLocalDataSource

    private lateinit var repository: SecurityStateRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(Dispatchers.Unconfined)

        repository = SecurityStateRepositoryImpl(mockRemoteDataSource, mockLocalDataSource)
    }

    @Test
    fun shouldGetSecurityStateFromRemote() = runBlocking {
        val expected = SecurityState(DEVICE_ID, SecurityState.Status.PROJECT_ENDED)
        coEvery { mockRemoteDataSource.getSecurityState() } returns expected

        val securityState = repository.getSecurityStatusFromRemote()

        assertThat(securityState).isEqualTo(expected)
    }

    @Test
    fun getSecurityState_shouldUpdateLocalDataSource() = runBlocking {
        val status = SecurityState.Status.RUNNING
        val state = SecurityState(DEVICE_ID, status)
        coEvery { mockRemoteDataSource.getSecurityState() } returns state

        repository.getSecurityStatusFromRemote()

        verify { mockLocalDataSource.securityStatus = status }
    }

    @Test(expected = SyncCloudIntegrationException::class)
    fun remoteDataSourceThrowsSyncCloudIntegrationException_repositoryShouldThrow() {
        coEvery {
            mockRemoteDataSource.getSecurityState()
        } throws SyncCloudIntegrationException(cause = Exception())

        runBlocking {
            repository.getSecurityStatusFromRemote()
        }
    }

    @Test(expected = HttpException::class)
    fun remoteDataSourceThrowsHttpException_repositoryShouldThrow() {
        coEvery { mockRemoteDataSource.getSecurityState() } throws HttpException(mockk())

        runBlocking {
            repository.getSecurityStatusFromRemote()
        }
    }

    @Test
    fun shouldSendSecurityStatusThroughChannel() = runBlocking {
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
