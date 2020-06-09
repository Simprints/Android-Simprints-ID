package com.simprints.id.secure.securitystate.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.id.exceptions.safe.data.db.SimprintsInternalServerException
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class SecurityStateRepositoryImplTest {

    @MockK lateinit var mockRemoteDataSource: SecurityStateRemoteDataSource

    private lateinit var repository: SecurityStateRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        repository = SecurityStateRepositoryImpl(mockRemoteDataSource)
    }

    @Test
    fun shouldGetSecurityStateFromRemote() = runBlocking {
        // TODO: replace empty string with SecurityState
        coEvery { mockRemoteDataSource.getSecurityState() } returns ""

        val securityState = repository.getSecurityState()

        assertThat(securityState).isEqualTo("")
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

}
