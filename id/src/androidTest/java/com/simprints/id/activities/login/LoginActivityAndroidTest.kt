package com.simprints.id.activities.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.activities.login.repository.LoginRepository
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.commontesttools.di.TestAppModule
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
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class LoginActivityAndroidTest {

    @Inject lateinit var mockCrashReportManager: CrashReportManager
    @Inject lateinit var mockRepository: LoginRepository

    @MockK lateinit var mockLoginActivityHelper: LoginActivityHelper

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val appModule by lazy {
        TestAppModule(app, crashReportManagerRule = DependencyRule.MockkRule)
    }

    private val loginModule by lazy {
        TestLoginModule(
            loginViewModelFactoryRule = DependencyRule.ReplaceRule {
                LoginViewModelFactory(mockRepository)
            },
            loginActivityHelperRule = DependencyRule.ReplaceRule {
                mockLoginActivityHelper
            },
            loginRepositoryRule = DependencyRule.MockkRule
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        AndroidTestConfig(this, appModule = appModule, loginModule = loginModule)
            .initAndInjectComponent()
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
            withMandatoryCredentialsMissing()
        } clickSignIn {
            assertMissingCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun typeProjectIdDifferentFromProvidedThroughIntent_clickSignIn_shouldShowToast() {
        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdNotMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
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
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            assertAlertScreenIsLaunched()
        }
    }

    @Test
    fun scannerAppIsInstalled_clickScanQr_shouldOpenScannerApp() {
        loginActivity {
            withScannerAppInstalled()
        } clickScanQr {
            assertScannerAppIsLaunched()
        }
    }

    @Test
    fun scannerAppNotInstalled_clickScanQr_shouldOpenScannerAppPlayStorePage() {
        loginActivity {
            withScannerAppNotInstalled()
        } clickScanQr {
            assertScannerAppPlayStorePageIsOpened()
        }
    }

    @Test
    fun receiveValidQrCodeResponse_shouldFillProjectIdAndProjectSecretFields() {
        loginActivity {
            withScannerAppInstalled()
            receiveValidQrCodeResponse()
        } clickScanQr {
            assertProjectIdFieldHasText(VALID_PROJECT_ID)
            assertProjectSecretFieldHasText(VALID_PROJECT_SECRET)
        }
    }

    @Test
    fun receiveInvalidQrCodeResponse_shouldShowToast() {
        loginActivity {
            withScannerAppInstalled()
            receiveInvalidQrCodeResponse()
        } clickScanQr {
            assertInvalidQrCodeToastIsDisplayed()
        }
    }

    @Test
    fun receiveErrorFromScannerApp_shouldShowToast() {
        loginActivity {
            withScannerAppInstalled()
            receiveErrorFromScannerApp()
        } clickScanQr {
            assertQrCodeErrorToastIsDisplayed()
        }
    }

    @Test
    fun pressBack_shouldReturnIntentWithLoginNotCompleted() {
        loginActivity {
        } pressBack {
            assertLoginNotCompleteIntentIsReturned()
        }
    }

    @Test
    fun clickScanQrButton_shouldLogToCrashReport() {
        loginActivity {
        } clickScanQr {
            assertMessageIsLoggedToCrashReport("Scan QR button clicked")
        }
    }

    @Test
    fun clickSignInButton_shouldLogToCrashReport() {
        loginActivity {
        } clickSignIn {
            assertMessageIsLoggedToCrashReport("Login button clicked")
        }
    }

    @Test
    fun receiveValidQrCodeResponse_shouldLogToCrashReport() {
        loginActivity {
            withScannerAppInstalled()
            receiveValidQrCodeResponse()
        } clickScanQr {
            assertMessageIsLoggedToCrashReport("QR scanning successful")
        }
    }

    @Test
    fun receiveInvalidQrCodeResponse_shouldLogToCrashReport() {
        loginActivity {
            withScannerAppInstalled()
            receiveInvalidQrCodeResponse()
        } clickScanQr {
            assertMessageIsLoggedToCrashReport("QR scanning unsuccessful")
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
