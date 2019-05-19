package com.simprints.id.activities.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
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
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityAndroidTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private var secureApiInterfaceRule: DependencyRule = DependencyRule.RealRule

    private val module
        get() = TestAppModule(app,
            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
            keystoreManagerRule = DependencyRule.ReplaceRule { mock<KeystoreManager>().apply { setupFakeKeyStore(this) } },
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
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchLoginActivity(invalidCredentials)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidCredentials_shouldFail() {
        launchLoginActivity(invalidCredentials)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validCredentialsWithoutInternet_shouldFail() {
        secureApiInterfaceRule = DependencyRule.ReplaceRule { replaceSecureApiClientWithFailingClientProvider() }
        AndroidTestConfig(this, module).initAndInjectComponent()

        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    companion object {
        private val invalidCredentials = TestCalloutCredentials(
            "beefdeadbeefdeadbeef",
            DEFAULT_MODULE_ID,
            DEFAULT_USER_ID)

        private const val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"
    }
}
