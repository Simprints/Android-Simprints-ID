package com.simprints.id.activities.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import com.simprints.core.analytics.CrashReportManager
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.eventsystem.event.domain.models.AuthenticationEvent.AuthenticationPayload.Result.*
import com.simprints.id.Application
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.di.TestSecurityModule
import com.simprints.id.commontesttools.di.TestViewModelModule
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class LoginActivityAndroidTest {

    @Inject
    lateinit var mockCrashReportManager: CrashReportManager

    @Inject
    lateinit var mockAuthenticationHelper: AuthenticationHelper

    @MockK
    lateinit var mockLoginActivityHelper: LoginActivityHelper

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val appModule by lazy {
        TestAppModule(app, crashReportManagerRule = DependencyRule.MockkRule)
    }

    private val securityModule by lazy {
        TestSecurityModule(
            loginActivityHelperRule = DependencyRule.ReplaceRule {
                mockLoginActivityHelper
            },
            authenticationHelperRule = DependencyRule.MockkRule
        )
    }

    private val viewModelModule by lazy {
        TestViewModelModule(
            loginViewModelFactoryRule = DependencyRule.ReplaceRule {
                LoginViewModelFactory(mockAuthenticationHelper)
            }
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        AndroidTestConfig(this, appModule = appModule, securityModule = securityModule, viewModelModule = viewModelModule)
            .initAndInjectComponent()
        Intents.init()
    }

    @Test
    fun userIdFieldShouldBeFilledInWithValueProvidedThroughIntent() {
        loginActivity {
        } assert {
            userIdFieldHasText(USER_ID)
        }
    }

    @Test
    fun withMissingCredentials_clickSignIn_shouldShowToast() {
        loginActivity {
            withMandatoryCredentialsMissing()
            withSecurityStatusRunning()
        } clickSignIn {
            missingCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun typeProjectIdDifferentFromProvidedThroughIntent_clickSignIn_shouldShowToast() {
        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdNotMatching()
            typeProjectId("invalid_project_id")
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            projectIdMismatchToastIsDisplayed()
        }
    }

    @Test
    fun typeValidCredentials_clickSignIn_shouldBeAuthenticated() {
        mockAuthenticationResult(AUTHENTICATED)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            userIsSignedIn()
        }
    }

    @Test
    fun whenOffline_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(OFFLINE)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            offlineToastIsDisplayed()
        }
    }

    @Test
    fun typeInvalidCredentials_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(BAD_CREDENTIALS)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            invalidCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun withServerError_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(TECHNICAL_FAILURE)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            serverErrorToastIsDisplayed()
        }
    }

    @Test
    fun withInvalidSafetyNetClaims_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(SAFETYNET_INVALID_CLAIM)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            alertScreenIsLaunched()
        }
    }

    @Test
    fun withSafetyNetUnavailable_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(SAFETYNET_UNAVAILABLE)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            alertScreenIsLaunched()
        }
    }

    @Test
    fun withUnknownError_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(UNKNOWN)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
            withSecurityStatusRunning()
        } clickSignIn {
            alertScreenIsLaunched()
        }
    }

    @Test
    fun clickScanQr_shouldOpenQrCaptureActivity() {
        loginActivity {
        } clickScanQr {
            qrCaptureActivityIsOpened()
        }
    }

    @Test
    fun receiveValidQrCodeResponse_shouldFillProjectIdAndProjectSecretFields() {
        loginActivity {
            receiveValidQrCodeResponse()
        } clickScanQr {
            projectIdFieldHasText(VALID_PROJECT_ID)
            projectSecretFieldHasText(VALID_PROJECT_SECRET)
        }
    }

    @Test
    fun receiveInvalidQrCodeResponse_shouldShowToast() {
        loginActivity {
            receiveInvalidQrCodeResponse()
        } clickScanQr {
            invalidQrCodeToastIsDisplayed()
        }
    }

    @Test
    fun receiveErrorFromScannerApp_shouldShowToast() {
        loginActivity {
            receiveQrScanError()
        } clickScanQr {
            qrCodeErrorToastIsDisplayed()
        }
    }

    @Test
    fun pressBack_shouldReturnIntentWithLoginNotCompleted() {
        loginActivity {
        } pressBack {
            loginNotCompleteIntentIsReturned()
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun mockAuthenticationResult(result: AuthenticationPayload.Result) {
        coEvery {
            mockAuthenticationHelper.authenticateSafely(any(), any(), any(), any())
        } returns result
    }

}
