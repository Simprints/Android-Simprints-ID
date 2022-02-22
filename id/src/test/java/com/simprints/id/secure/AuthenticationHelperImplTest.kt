package com.simprints.id.secure

import com.google.common.truth.Truth.assertThat
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result
import com.simprints.id.exceptions.safe.BackendMaintenanceException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthenticationHelperImplTest {

    private lateinit var authenticationHelperImpl: AuthenticationHelperImpl
    private val loginInfoManager: LoginInfoManager = mockk(relaxed = true)
    private val timeHelper: TimeHelper = mockk(relaxed = true)
    private val projectAuthenticator: ProjectAuthenticator = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        authenticationHelperImpl =
            AuthenticationHelperImpl(loginInfoManager, timeHelper, projectAuthenticator, eventRepository)
    }

    @Test
    fun shouldSetBackendErrorIfBackendMaintenanceException() = runBlocking {
        val result = mockException(BackendMaintenanceException())

        assertThat(result).isInstanceOf(Result.BACKEND_MAINTENANCE_ERROR::class.java)
    }

    @Test
    fun shouldSetOfflineIfIOException() = runBlocking {
        val result = mockException(IOException())

        assertThat(result).isInstanceOf(Result.OFFLINE::class.java)
    }

    private suspend fun mockException(exception: Exception): Result {
        coEvery { projectAuthenticator.authenticate(any(), "", "") } throws exception

        return authenticationHelperImpl.authenticateSafely("", "", "", "")
    }
}
