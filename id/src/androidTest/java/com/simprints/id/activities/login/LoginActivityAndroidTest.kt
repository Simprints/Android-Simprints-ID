package com.simprints.id.activities.login

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.commontesttools.di.TestLoginModule
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.domain.models.events.AuthenticationEvent
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityAndroidTest {

    @MockK lateinit var mockCrashReportManager: CrashReportManager
    @MockK lateinit var mockRepository: LoginRepository

    private val loginModule by lazy {
        TestLoginModule(loginViewModelFactoryRule = DependencyRule.ReplaceRule {
            LoginViewModelFactory(mockRepository)
        })
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        AndroidTestConfig(this, loginModule = loginModule).initAndInjectComponent()
        Intents.init()
    }

    @Test
    fun userIdFieldShouldBeFilledInWithValueProvidedThroughIntent() {
        loginActivity {
        } assert {
            assertUserIdFieldHasText(USER_ID)
        }
    }

    @Test
    fun withMissingCredentials_clickSignIn_shouldShowToast() {
        loginActivity {
        } clickSignIn {
            assertMissingCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun typeProjectIdDifferentFromProvidedThroughIntent_clickSignIn_shouldShowToast() {
        loginActivity {
            typeProjectId("invalid_project_id")
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertProjectIdMismatchToastIsDisplayed()
        }
    }

    @Test
    fun typeValidCredentials_clickSignIn_shouldBeAuthenticated() {
        mockAuthenticationResult(AuthenticationEvent.Result.AUTHENTICATED)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertUserIsSignedIn()
        }
    }

    @Test
    fun whenOffline_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticationEvent.Result.OFFLINE)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertOfflineToastIsDisplayed()
        }
    }

    @Test
    fun typeInvalidCredentials_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticationEvent.Result.BAD_CREDENTIALS)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertInvalidCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun withServerError_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticationEvent.Result.TECHNICAL_FAILURE)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertServerErrorToastIsDisplayed()
        }
    }

    @Test
    fun withInvalidSafetyNetClaims_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(AuthenticationEvent.Result.SAFETYNET_INVALID_CLAIM)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertAlertScreenIsLaunched()
        }
    }

    @Test
    fun withSafetyNetUnavailable_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(AuthenticationEvent.Result.SAFETYNET_UNAVAILABLE)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertAlertScreenIsLaunched()
        }
    }

    @Test
    fun withUnknownError_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(AuthenticationEvent.Result.UNKNOWN)

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertAlertScreenIsLaunched()
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun mockAuthenticationResult(result: AuthenticationEvent.Result) {
        coEvery { mockRepository.authenticate(any(), any(), any()) } returns result
    }

}
