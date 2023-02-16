package com.simprints.id.activities.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.simprints.id.R
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.models.AuthenticateDataResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class LoginActivityAndroidTest {

    @Inject
    lateinit var mockAuthenticationHelper: AuthenticationHelper

    @MockK
    lateinit var mockLoginActivityHelper: LoginActivityHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)


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
        } clickSignIn {
            projectIdMismatchToastIsDisplayed()
        }
    }

    @Test
    fun typeValidCredentials_clickSignIn_shouldBeAuthenticated() {
        mockAuthenticationResult(AuthenticateDataResult.Authenticated)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            userIsSignedIn()
        }
    }

    @Test
    fun whenOffline_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticateDataResult.Offline)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            offlineToastIsDisplayed()
        }
    }

    @Test
    fun whenBackendMaintenance_clickSignIn_shouldShowTimedError() {
        mockAuthenticationResult(AuthenticateDataResult.BackendMaintenanceError(600L))

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            onView(withId(R.id.errorTextView)).check(
                matches(
                    ViewMatchers.withText(
                        SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_TIMED_MESSAGE
                    )
                )
            )
            onView(withId(R.id.errorTextView)).check(matches(isDisplayed()))
            onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun whenBackendMaintenance_clickSignIn_shouldShowError() {
        mockAuthenticationResult(AuthenticateDataResult.BackendMaintenanceError())

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            onView(withId(R.id.errorTextView)).check(
                matches(
                    ViewMatchers.withText(
                        SYNC_CARD_FAILED_BACKEND_MAINTENANCE_STATE_MESSAGE
                    )
                )
            )
            onView(withId(R.id.errorTextView)).check(matches(isDisplayed()))
            onView(withId(R.id.errorCard)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun typeInvalidCredentials_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticateDataResult.BadCredentials)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            invalidCredentialsToastIsDisplayed()
        }
    }

    @Test
    fun withServerError_clickSignIn_shouldShowToast() {
        mockAuthenticationResult(AuthenticateDataResult.TechnicalFailure)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            serverErrorToastIsDisplayed()
        }
    }

    @Test
    fun withIntegrityUnavailable_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(AuthenticateDataResult.IntegrityException)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
        } clickSignIn {
            alertScreenIsLaunched()
        }
    }

    @Test
    fun withUnknownError_clickSignIn_shouldLaunchAlertScreen() {
        mockAuthenticationResult(AuthenticateDataResult.Unknown)

        loginActivity {
            withMandatoryCredentialsPresent()
            withSuppliedProjectIdAndIntentProjectIdMatching()
            typeProjectId(VALID_PROJECT_ID)
            typeProjectSecret(VALID_PROJECT_SECRET)
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

    private fun mockAuthenticationResult(result: AuthenticateDataResult) {
        coEvery {
            mockAuthenticationHelper.authenticateSafely(any(), any(), any(), any())
        } returns result
    }

}
