package com.simprints.id.secure.securitystate.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class SecurityStateRepositoryImplTest {

    @MockK lateinit var mockRemoteDataSource: SecurityStateRemoteDataSource
    @MockK lateinit var mockLocalDataSource: SecurityStateLocalDataSource
    @MockK lateinit var mockChannel: Channel<SecurityState.Status>

    private lateinit var repository: SecurityStateRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        repository = SecurityStateRepositoryImpl(mockRemoteDataSource, mockLocalDataSource).apply {
            securityStatusChannel = mockChannel
        }
    }

    @Test
    fun shouldGetSecurityStateFromRemote() = runBlocking {
        val expected = SecurityState(DEVICE_ID, SecurityState.Status.PROJECT_ENDED)
        coEvery { mockRemoteDataSource.getSecurityState() } returns expected

        val securityState = repository.getSecurityState()

        assertThat(securityState).isEqualTo(expected)
    }

    @Test
    fun getSecurityState_shouldUpdateLocalDataSource() = runBlocking {
        val status = SecurityState.Status.RUNNING
        val state = SecurityState(DEVICE_ID, status)
        coEvery { mockRemoteDataSource.getSecurityState() } returns state

        repository.getSecurityState()

        verify { mockLocalDataSource.setSecurityStatus(status) }
    }

    @Test(expected = SimprintsInternalServerException::class)
    fun remoteDataSourceThrowsInternalServerException_repositoryShouldThrow() {
        coEvery {
            mockRemoteDataSource.getSecurityState()
        } throws SimprintsInternalServerException()

        runBlocking {
            repository.getSecurityState()
        }
    }

    @Test(expected = HttpException::class)
    fun remoteDataSourceThrowsHttpException_repositoryShouldThrow() {
        coEvery { mockRemoteDataSource.getSecurityState() } throws HttpException(mockk())

        runBlocking {
            repository.getSecurityState()
        }
    }

    @Test
    fun shouldSendSecurityStatusThroughChannel() = runBlocking {
        val securityState = SecurityState(DEVICE_ID, SecurityState.Status.PROJECT_ENDED)
        coEvery { mockRemoteDataSource.getSecurityState() } returns securityState

        repository.getSecurityState()

        coVerify { mockChannel.send(securityState.status) }
    }

    private companion object {
        const val DEVICE_ID = "mock-device-id"
    }

}
