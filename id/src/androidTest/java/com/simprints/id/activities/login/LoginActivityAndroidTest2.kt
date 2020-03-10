package com.simprints.id.activities.login

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
import org.junit.Before
import org.junit.Test

class LoginActivityAndroidTest2 {

    @MockK lateinit var mockCrashReportManager: CrashReportManager
    @MockK lateinit var mockRepository: LoginRepository

    private val loginModule by lazy {
        TestLoginModule(loginViewModelFactoryRule = DependencyRule.ReplaceRule {
            LoginViewModelFactory(mockRepository)
        })
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        AndroidTestConfig(this, loginModule = loginModule).initAndInjectComponent()
    }

    @Test
    fun userIdFieldShouldBeFilledInWithValueProvidedThroughIntent() {
        loginActivity {
        } assert {
            assertUserIdFieldHasText(USER_ID)
        }
    }

    @Test
    fun typeValidCredentials_clickSignIn_shouldBeAuthenticated() {
        coEvery {
            mockRepository.authenticate(any(), any(), any())
        } returns AuthenticationEvent.Result.AUTHENTICATED

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertUserIsSignedIn()
        }
    }

    @Test
    fun typeInvalidCredentials_clickSignIn_shouldShowToast() {
        coEvery {
            mockRepository.authenticate(any(), any(), any())
        } returns AuthenticationEvent.Result.BAD_CREDENTIALS

        loginActivity {
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertInvalidCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun typeProjectIdDifferentFromProvidedThroughIntent_clickSignIn_shouldShowToast() {
        coEvery {
            mockRepository.authenticate(any(), any(), any())
        } returns AuthenticationEvent.Result.BAD_CREDENTIALS

        loginActivity {
            typeProjectId("invalid_project_id")
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertProjectIdMismatchToastIsDisplayed()
        }
    }



}
