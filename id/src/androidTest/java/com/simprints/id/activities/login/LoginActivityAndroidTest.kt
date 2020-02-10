package com.simprints.id.activities.login

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.commontesttools.state.replaceSecureApiClientWithFailingClientProvider
import com.simprints.id.commontesttools.state.setupFakeKeyStore
import com.simprints.id.commontesttools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.android.BaseAssertions
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginActivityAndroidTest : BaseAssertions() {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private var secureApiInterfaceRule: DependencyRule = DependencyRule.RealRule

    private val module
        get() = TestAppModule(app,
            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
            keystoreManagerRule = DependencyRule.ReplaceRule { mockk<KeystoreManager>().apply { setupFakeKeyStore(this) } },
            secureApiInterfaceRule = secureApiInterfaceRule
        )

    @Before
    fun setUp() {
        AndroidTestConfig(this, module).fullSetup()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        val scenario = launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess(scenario)
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFailWithToast() {
        launchLoginActivity(invalidCredentials)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        assertToastMessageIs(R.string.login_invalid_credentials)
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFailWithToast() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        assertToastMessageIs(R.string.login_project_id_intent_mismatch)
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFailWithToast() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()

        assertToastMessageIs(R.string.login_invalid_credentials)
    }

    @Test
    fun invalidCredentials_shouldFailWithToast() {
        launchLoginActivity(invalidCredentials)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()

        assertToastMessageIs(R.string.login_invalid_credentials)
    }

    @Test
    fun validCredentialsWithoutInternet_shouldFailWithToast() {
        secureApiInterfaceRule = DependencyRule.ReplaceRule { replaceSecureApiClientWithFailingClientProvider() }
        AndroidTestConfig(this, module).initAndInjectComponent()

        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()

        assertToastMessageIs(R.string.login_no_network)
    }

    @Test
    fun pressBack_shouldFinishWithRightResult() {
        val activityScenario = launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        Espresso.pressBackUnconditionally()

        verifyIntentReturnedForLoginNotComplete(activityScenario.result)
    }

    private fun verifyIntentReturnedForLoginNotComplete(result: Instrumentation.ActivityResult) {
        assertThat(result.resultCode).isEqualTo(Activity.RESULT_OK)

        result.resultData.setExtrasClassLoader(AppErrorResponse::class.java.classLoader)
        val response = result.resultData.getParcelableExtra<AppErrorResponse>(LoginActivityResponse.BUNDLE_KEY)

        assertThat(response.reason).isEqualTo(AppErrorResponse.Reason.LOGIN_NOT_COMPLETE)
    }

    companion object {
        private val invalidCredentials = TestCalloutCredentials(
            "beefdeadbeefdeadbeef",
            DEFAULT_MODULE_ID,
            DEFAULT_USER_ID)

        private const val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
    }
}
