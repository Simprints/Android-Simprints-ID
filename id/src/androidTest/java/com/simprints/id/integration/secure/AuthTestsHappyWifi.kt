package com.simprints.id.integration.secure

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.simprints.id.Application
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_SECRET
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_REALM_KEY
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_TEST_CALLOUT_CREDENTIALS
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.di.DependencyRule.MockRule
import com.simprints.id.commontesttools.di.DependencyRule.ReplaceRule
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.commontesttools.models.TestCalloutCredentials
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.integration.testsnippets.*
import com.simprints.id.testtools.AndroidTestConfig
import com.simprints.id.testtools.checkLoginFromIntentActivityTestRule
import com.simprints.id.testtools.state.setupRandomGeneratorToGenerateKey
import com.simprints.id.tools.RandomGenerator
import com.simprints.mockscanner.MockBluetoothAdapter
import com.simprints.mockscanner.MockScannerManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class AuthTestsHappyWifi {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val invalidCredentials = TestCalloutCredentials(
        "beefdeadbeefdeadbeef",
        DEFAULT_MODULE_ID,
        DEFAULT_USER_ID,
        "deadbeef-dead-beef-dead-deaddeadbeef"
    )

    private val invalidSecret = "deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef"

    @get:Rule val loginTestRule = checkLoginFromIntentActivityTestRule()

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var randomGeneratorMock: RandomGenerator

    private val module by lazy {
        TestAppModule(app,
            randomGeneratorRule = MockRule,
            bluetoothComponentAdapterRule = ReplaceRule { mockBluetoothAdapter })
    }

    private val mockBluetoothAdapter = MockBluetoothAdapter(MockScannerManager())

    @Before
    fun setUp() {
        AndroidTestConfig(this, module).fullSetup()

        setupRandomGeneratorToGenerateKey(DEFAULT_REALM_KEY, randomGeneratorMock)

        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentials_shouldSucceed() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun invalidIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentLegacyProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validIntentProjectIdAndInvalidSubmittedProjectId_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validLegacyProjectIdAndInvalidSecret_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        enterCredentialsDirectly(invalidCredentials, invalidSecret)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun invalidLegacyCredentials_shouldFail() {
        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        enterCredentialsDirectly(invalidCredentials, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInFailure()
    }

    @Test
    fun validCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrolAndDoLogin(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule, DEFAULT_PROJECT_SECRET)

        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validLegacyCredentials_shouldPersistAcrossAppRestart() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        ensureSignInSuccess()
        signOut()
    }

    @Test
    fun validCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrolAndDoLogin(DEFAULT_TEST_CALLOUT_CREDENTIALS, loginTestRule, DEFAULT_PROJECT_SECRET)

        launchAppFromIntentEnrol(invalidCredentials, loginTestRule)
        ensureConfigError()
        signOut()
    }

    @Test
    fun validLegacyCredentialsThenRestartingWithInvalidCredentials_shouldFail() {
        launchAppFromIntentEnrol(DEFAULT_TEST_CALLOUT_CREDENTIALS.toLegacy(), loginTestRule)
        enterCredentialsDirectly(DEFAULT_TEST_CALLOUT_CREDENTIALS, DEFAULT_PROJECT_SECRET)
        pressSignIn()
        ensureSignInSuccess()

        launchAppFromIntentEnrol(invalidCredentials.toLegacy(), loginTestRule)
        ensureConfigError()
        signOut()
    }

    private fun signOut() {
        remoteDbManager.signOutOfRemoteDb()
    }
}
