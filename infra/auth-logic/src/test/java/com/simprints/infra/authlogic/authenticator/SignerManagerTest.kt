package com.simprints.infra.authlogic.authenticator

import com.simprints.infra.authlogic.worker.SecurityStateScheduler
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.images.ImageUpSyncScheduler
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class SignerManagerTest {

    @MockK
    lateinit var mockConfigManager: ConfigManager

    @MockK
    lateinit var mockAuthStore: AuthStore

    @MockK
    lateinit var mockEventSyncManager: EventSyncManager

    @MockK
    lateinit var mockSecurityStateScheduler: SecurityStateScheduler

    @MockK
    lateinit var mockRecentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var mockImageUpSyncScheduler: ImageUpSyncScheduler

    @MockK
    lateinit var mockSimNetwork: SimNetwork

    private lateinit var signerManager: SignerManager

    private val token = Token(
        "some_token",
        "some_project_id",
        "some_api_key",
        "some_application_id"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        signerManager = SignerManager(
            mockConfigManager,
            mockAuthStore,
            mockEventSyncManager,
            mockSecurityStateScheduler,
            mockRecentUserActivityManager,
            mockImageUpSyncScheduler,
            mockSimNetwork,
            UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun signIn_shouldSignInToRemoteDb() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockFetchingProjectInfo()
        mockFetchingProjectConfiguration()

        signIn()

        coVerify { mockAuthStore.storeFirebaseToken(token) }
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
        mockFetchingProjectConfiguration()

        signIn()

        verify { mockAuthStore.storeCredentials(DEFAULT_PROJECT_ID) }
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
        mockFetchingProjectConfiguration()

        signIn()

        coVerify { mockConfigManager.refreshProject(DEFAULT_PROJECT_ID) }
    }

    @Test
    fun refreshProjectInfoFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo(true)
        mockFetchingProjectConfiguration()

        assertThrows<Throwable> { signIn() }

        verify { mockAuthStore.clearFirebaseToken() }
        coVerify { mockConfigManager.clearData() }
        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
        verify { mockAuthStore.cleanCredentials() }
    }

    @Test
    fun refreshProjectConfigurationFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockFetchingProjectConfiguration(true)

        assertThrows<Throwable> { signIn() }

        verify { mockAuthStore.clearFirebaseToken() }
        coVerify { mockConfigManager.clearData() }
        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
        verify { mockAuthStore.cleanCredentials() }
    }

    @Test
    fun signIn_shouldSucceed() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockFetchingProjectConfiguration()

        signIn()
    }

    @Test
    fun signIn_shouldScheduleSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockFetchingProjectConfiguration()

        signIn()

        verify { mockSecurityStateScheduler.scheduleSecurityStateCheck() }
    }

    @Test
    fun signOut_shouldRemoveAnyState() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verifyStoredCredentialsGotCleaned()
        verifyRemoteManagerGotSignedOut()
        verifyLastSyncInfoGotDeleted()
        coVerify(exactly = 1) { mockConfigManager.clearData() }
    }

    @Test
    fun signOut_shouldCancelPeriodicSecurityStateCheck() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
    }

    @Test
    fun signOut_backgroundSyncWorkersAreCancelled() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockEventSyncManager.cancelScheduledSync() }
        verify { mockImageUpSyncScheduler.cancelImageUpSync() }
        coVerify { mockConfigManager.cancelScheduledSyncConfiguration() }
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

        coVerify { mockConfigManager.clearData() }
    }

    @Test
    fun `when signedInProjectId is accessed then authStore is invoked`() {
        signerManager.signedInProjectId

        verify(exactly = 1) { mockAuthStore.signedInProjectId }
    }

    private suspend fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, token)

    private fun mockStoreCredentialsLocally(error: Boolean = false) =
        every { mockAuthStore.storeCredentials(DEFAULT_PROJECT_ID) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to store credentials"))
            }
        }

    private fun mockRemoteSignedIn(error: Boolean = false) =
        coEvery { mockAuthStore.storeFirebaseToken(token) }.apply {
            if (error) {
                this.throws(Throwable("Failed to store credentials"))
            } else {
                this.returns(Unit)
            }
        }

    private fun mockFetchingProjectInfo(error: Boolean = false) =
        coEvery { mockConfigManager.refreshProject(any()) }.apply {
            if (!error) {
                this.returns(
                    Project(
                        DEFAULT_PROJECT_ID,
                        "local",
                        "",
                        "",
                        "some_bucket_url",
                        "",
                        tokenizationKeys = emptyMap()
                    )
                )
            } else {
                this.throws(Exception("Failed to fetch project info"))
            }
        }

    private fun mockFetchingProjectConfiguration(error: Boolean = false) =
        coEvery { mockConfigManager.refreshProjectConfiguration(any()) }.apply {
            if (!error) {
                this.returns(mockk())
            } else {
                this.throws(Exception("Failed to fetch project configuration"))
            }
        }

    private fun verifyStoredCredentialsGotCleaned() =
        verify { mockAuthStore.cleanCredentials() }

    private fun verifyRemoteManagerGotSignedOut() = verify { mockAuthStore.clearFirebaseToken() }
    private fun verifyLastSyncInfoGotDeleted() = coVerify { mockEventSyncManager.deleteSyncInfo() }
}
