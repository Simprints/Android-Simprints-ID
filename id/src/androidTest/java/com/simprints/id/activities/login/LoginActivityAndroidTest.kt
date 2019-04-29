package com.simprints.id.activities.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.commontesttools.state.setupFakeKeyStore
import com.simprints.id.commontesttools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.data.secure.keystore.KeystoreManager
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.tools.RandomGenerator
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityAndroidTest { // TODO : Failing since Sessions Realm is being decrypted with wrong key

    private val app = ApplicationProvider.getApplicationContext<Application>()

    @get:Rule val loginTestRule = ActivityTestRule(LoginActivity::class.java, false, false)

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = DependencyRule.ReplaceRule { mock<RandomGenerator>().apply { setupRandomGeneratorToGenerateKey(this) } },
            keystoreManagerRule = DependencyRule.ReplaceRule { mock<KeystoreManager>().apply { setupFakeKeyStore(this) } }
        )
    }

    @Before
    fun setUp() {
        AndroidTestConfig(this, module).fullSetup()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess(loginTestRule)
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchLoginActivity(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchLoginActivity(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidCredentials_shouldFail() {
        launchLoginActivity(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
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
