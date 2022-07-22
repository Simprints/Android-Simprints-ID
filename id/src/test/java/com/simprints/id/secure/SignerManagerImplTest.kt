package com.simprints.id.secure

import com.simprints.core.login.LoginInfoManager
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.secure.models.Token
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SignerManagerImplTest {

    @MockK
    lateinit var mockProjectRepository: ProjectRepository

    @MockK
    lateinit var mockRemoteDbManager: RemoteDbManager

    @MockK
    lateinit var mockLoginInfoManager: LoginInfoManager

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
    lateinit var mockBaseUrlProvider: BaseUrlProvider

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
            mockProjectRepository,
            mockRemoteDbManager,
            mockLoginInfoManager,
            mockPreferencesManager,
            mockEventSyncManager,
            mockSyncManager,
            mockSecurityStateScheduler,
            mockLongConsentRepository,
            mockBaseUrlProvider,
            mockRemoteConfigWrapper
        )
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSignInToRemoteDb() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockFetchingProjectInfo()

        signIn()

        coVerify { mockRemoteDbManager.signIn(token) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signInToRemoteFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldStoreCredentialsLocally() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        verify { mockLoginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun storeCredentialsFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldFetchProjectInfo() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        coVerify { mockProjectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun loadAndRefreshCacheFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSucceed() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldScheduleSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()

        verify { mockSecurityStateScheduler.scheduleSecurityStateCheck() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_shouldRemoveAnyState() = runTest(UnconfinedTestDispatcher()) {
        every { mockLoginInfoManager.signedInProjectId } returns DEFAULT_PROJECT_ID

        signerManager.signOut()

        verifyUpSyncGotPaused()
        verifyStoredCredentialsGotCleaned()
        verifyRemoteManagerGotSignedOut()
        verifyLastSyncInfoGotDeleted()
        verifyAllSharedPreferencesExceptRealmKeysGotCleared()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_shouldCancelPeriodicSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        every { mockLoginInfoManager.signedInProjectId } returns DEFAULT_PROJECT_ID

        signerManager.signOut()

        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_backgroundSyncWorkersAreCancelled() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockSyncManager.cancelBackgroundSyncs() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_longConsentsAreDeleted() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify(exactly = 1) { mockLongConsentRepository.deleteLongConsents() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_apiBaseUrlIsReset() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockBaseUrlProvider.resetApiBaseUrl() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_clearRemoteConfig() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockRemoteConfigWrapper.clearRemoteConfig() }
    }

    private suspend fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, token)

    private fun mockStoreCredentialsLocally(error: Boolean = false) =
        every { mockLoginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to store credentials"))
            }
        }

    private fun mockRemoteSignedIn(error: Boolean = false) =
        coEvery { mockRemoteDbManager.signIn(token) }.apply {
            if (error) {
                this.throws(Throwable("Failed to store credentials"))
            } else {
                this.returns(Unit)
            }
        }

    private fun mockFetchingProjectInfo(error: Boolean = false) =
        coEvery { mockProjectRepository.loadFromRemoteAndRefreshCache(any()) }.apply {
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
        verify { mockLoginInfoManager.cleanCredentials() }

    private fun verifyRemoteManagerGotSignedOut() = verify { mockRemoteDbManager.signOut() }
    private fun verifyLastSyncInfoGotDeleted() = coVerify { mockEventSyncManager.deleteSyncInfo() }
    private fun verifyAllSharedPreferencesExceptRealmKeysGotCleared() =
        verify { mockPreferencesManager.clearAllSharedPreferences() }
}
