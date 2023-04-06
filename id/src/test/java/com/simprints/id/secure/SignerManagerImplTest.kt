package com.simprints.id.secure

import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
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
    lateinit var mockSyncManager: SyncManager

    @MockK
    lateinit var mockEventSyncManager: EventSyncManager

    @MockK
    lateinit var mockSecurityStateScheduler: SecurityStateScheduler

    @MockK
    lateinit var mockRecentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var mockSimNetwork: SimNetwork

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
            mockEventSyncManager,
            mockSyncManager,
            mockSecurityStateScheduler,
            mockRecentUserActivityManager,
            mockSimNetwork,
            UnconfinedTestDispatcher(),
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
        coVerify(exactly = 1) { configManager.clearData() }
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
    fun signOut_apiBaseUrlIsReset() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockSimNetwork.resetApiBaseUrl() }
    }

    @Test
    fun signOut_recentActivityIsCleared() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockRecentUserActivityManager.clearRecentActivity() }
    }

    @Test
    fun signOut_clearConfiguration() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { configManager.clearData() }
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
}
