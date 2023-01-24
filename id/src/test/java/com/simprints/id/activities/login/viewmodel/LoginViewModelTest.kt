package com.simprints.id.activities.login.viewmodel


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.models.AuthenticateDataResult
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.livedata.getOrAwaitValue
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class LoginViewModelTest {

    private lateinit var loginViewModel: LoginViewModel

    @MockK
    lateinit var authenticationHelper: AuthenticationHelper

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        loginViewModel = LoginViewModel(authenticationHelper)
    }


    @Test
    fun signIn() = runBlocking {
        //Given
        coEvery {
            authenticationHelper.authenticateSafely(
                "userId", "projectId", "projectSecret", "deviceId"
            )
        } returns AuthenticateDataResult.Authenticated

        //When
        loginViewModel.signIn(
            "userId", "projectId", "projectSecret", "deviceId"
        )
        val result = loginViewModel.getSignInResult().getOrAwaitValue()
        //Then
        assertThat(result)
            .isEqualTo(AuthenticateDataResult.Authenticated)
    }

    @Test
    fun signIn_withBackendError() = runBlocking {
        //Given
        coEvery {
            authenticationHelper.authenticateSafely(
                "userId", "projectId", "projectSecret", "deviceId"
            )
        } returns AuthenticateDataResult.BackendMaintenanceError()

        //When
        loginViewModel.signIn(
            "userId", "projectId", "projectSecret", "deviceId"
        )
        val result = loginViewModel.getSignInResult().getOrAwaitValue()
        //Then
        assertThat(result)
            .isEqualTo(AuthenticateDataResult.BackendMaintenanceError())
    }

    @Test
    fun signIn_withTimedBackendError() = runBlocking {
        //Given
        coEvery {
            authenticationHelper.authenticateSafely(
                "userId", "projectId", "projectSecret", "deviceId"
            )
        } returns AuthenticateDataResult.BackendMaintenanceError(600L)

        //When
        loginViewModel.signIn(
            "userId", "projectId", "projectSecret", "deviceId"
        )
        val result = loginViewModel.getSignInResult().getOrAwaitValue()
        //Then
        assertThat(result)
            .isEqualTo(AuthenticateDataResult.BackendMaintenanceError(600L))
        assertThat((result as AuthenticateDataResult.BackendMaintenanceError).estimatedOutage).isEqualTo(600L)
    }
}
