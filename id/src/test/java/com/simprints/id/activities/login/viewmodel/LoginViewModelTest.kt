package com.simprints.id.activities.login.viewmodel


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
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
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        loginViewModel = LoginViewModel(authenticationHelper, testDispatcherProvider)
    }


    @Test
    fun signIn() = runBlocking {
        //Given
        coEvery {
            authenticationHelper.authenticateSafely(
                "userId", "projectId", "projectSecret", "deviceId"
            )
        } returns AuthenticationEvent.AuthenticationPayload.Result.Authenticated

        //When
        loginViewModel.signIn(
            "userId", "projectId", "projectSecret", "deviceId"
        )
        val result = loginViewModel.getSignInResult().getOrAwaitValue()
        //Then
        Truth.assertThat(result)
            .isEqualTo(AuthenticationEvent.AuthenticationPayload.Result.Authenticated)
    }
}
