package com.simprints.id.secure

import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignerManagerImplTest {

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var mockLoginManager: LoginManager

    @MockK
    lateinit var mockPreferencesManager: PreferencesManager

    @MockK
    lateinit var mockSyncManager: SyncManager

    @MockK
    lateinit var mockEventSyncManager: EventSyncManager

    @MockK
    lateinit var mockSecurityStateScheduler: SecurityStateScheduler

    @MockK
    lateinit var mockLongConsentRepository: LongConsentRepository

    @MockK
    lateinit var mockSimNetwork: SimNetwork

    @MockK
    lateinit var mockRemoteConfigWrapper: RemoteConfigWrapper

    private lateinit var signerManager: SignerManagerImpl

    private val token = Token(
        "some_token",
        "some_project_id",
        "some_api_key",
        "some_application_id"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        signerManager = SignerManagerImpl(
            configManager,
            mockLoginManager,
            mockPreferencesManager,
            mockEventSyncManager,
            mockSyncManager,
            mockSecurityStateScheduler,
            mockLongConsentRepository,
            mockSimNetwork,
            mockRemoteConfigWrapper
        )
    }

    @Test
    fun signIn_shouldSignInToRemoteDb() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockFetchingProjectInfo()

        signIn()

        coVerify { mockLoginManager.signIn(token) }
    }

    @Test
    fun signInToRemoteFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    fun signIn_shouldStoreCredentialsLocally() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        verify { mockLoginManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }
    }

    @Test
    fun storeCredentialsFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    fun signIn_shouldFetchProjectInfo() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        coVerify { configManager.refreshProject(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun loadAndRefreshCacheFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    fun signIn_shouldSucceed() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()
    }

    @Test
    fun signIn_shouldScheduleSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()

        verify { mockSecurityStateScheduler.scheduleSecurityStateCheck() }
    }

    @Test
    fun signOut_shouldRemoveAnyState() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verifyUpSyncGotPaused()
        verifyStoredCredentialsGotCleaned()
        verifyRemoteManagerGotSignedOut()
        verifyLastSyncInfoGotDeleted()
        verifyAllSharedPreferencesExceptRealmKeysGotCleared()
    }

    @Test
    fun signOut_shouldCancelPeriodicSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
    }

    @Test
    fun signOut_backgroundSyncWorkersAreCancelled() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockSyncManager.cancelBackgroundSyncs() }
    }

    @Test
    fun signOut_longConsentsAreDeleted() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify(exactly = 1) { mockLongConsentRepository.deleteLongConsents() }
    }

    @Test
    fun signOut_apiBaseUrlIsReset() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockSimNetwork.resetApiBaseUrl() }
    }

    @Test
    fun signOut_clearRemoteConfig() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockRemoteConfigWrapper.clearRemoteConfig() }
    }

    private suspend fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, token)

    private fun mockStoreCredentialsLocally(error: Boolean = false) =
        every { mockLoginManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to store credentials"))
            }
        }

    private fun mockRemoteSignedIn(error: Boolean = false) =
        coEvery { mockLoginManager.signIn(token) }.apply {
            if (error) {
                this.throws(Throwable("Failed to store credentials"))
            } else {
                this.returns(Unit)
            }
        }

    private fun mockFetchingProjectInfo(error: Boolean = false) =
        coEvery { configManager.refreshProject(any()) }.apply {
            if (!error) {
                this.returns(
                    Project(
                        DEFAULT_PROJECT_ID,
                        "local",
                        "",
                        "",
                        "some_bucket_url"
                    )
                )
            } else {
                this.throws(Throwable("Failed to fetch project info"))
            }
        }

    private fun mockResumePeopleSync(error: Boolean = false) =
        coEvery { mockSyncManager.scheduleBackgroundSyncs() }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to resume upsync"))
            }
        }

    private fun verifyUpSyncGotPaused() = verify { mockSyncManager.cancelBackgroundSyncs() }
    private fun verifyStoredCredentialsGotCleaned() =
        verify { mockLoginManager.cleanCredentials() }

    private fun verifyRemoteManagerGotSignedOut() = verify { mockLoginManager.signOut() }
    private fun verifyLastSyncInfoGotDeleted() = coVerify { mockEventSyncManager.deleteSyncInfo() }
    private fun verifyAllSharedPreferencesExceptRealmKeysGotCleared() =
        verify { mockPreferencesManager.clearAllSharedPreferences() }
}
