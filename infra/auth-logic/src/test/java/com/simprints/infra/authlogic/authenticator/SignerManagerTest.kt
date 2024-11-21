package com.simprints.infra.authlogic.authenticator

import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.models.ProjectState
import com.simprints.infra.config.store.models.ProjectWithConfig
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.license.LicenseRepository
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
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var mockAuthStore: AuthStore

    @MockK
    lateinit var mockRecentUserActivityManager: RecentUserActivityManager

    @MockK
    lateinit var mockSimNetwork: SimNetwork

    @MockK
    lateinit var mockEventRepository: EventRepository

    @MockK
    lateinit var mockImageRepository: ImageRepository

    @MockK
    lateinit var mockLicenseRepository: LicenseRepository

    @MockK
    lateinit var mockEnrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var scannerManager: ScannerManager

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
            configManager,
            mockAuthStore,
            mockRecentUserActivityManager,
            mockSimNetwork,
            mockImageRepository,
            mockEventRepository,
            mockEnrolmentRecordRepository,
            mockLicenseRepository,
            scannerManager,
            UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun signIn_shouldSignInToRemoteDb() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockFetchingProjectInfo()

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

        signIn()

        verify { mockAuthStore.signedInProjectId = DEFAULT_PROJECT_ID }
    }

    @Test
    fun storeCredentialsFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        every { mockAuthStore.signedInProjectId = any() } throws Throwable("Failed to store credentials")

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
    fun refreshProjectInfoFails_signInShouldFail() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo(true)

        assertThrows<Throwable> { signIn() }

        verify { mockAuthStore.clearFirebaseToken() }
        coVerify { configManager.clearData() }
        verify { mockAuthStore.cleanCredentials() }
    }

    @Test
    fun signIn_shouldSucceed() = runTest(UnconfinedTestDispatcher()) {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()
    }

    @Test
    fun signOut_shouldRemoveAnyState() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        verify { mockAuthStore.cleanCredentials() }
        verify { mockAuthStore.clearFirebaseToken() }
        coVerify(exactly = 1) { configManager.clearData() }
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

    @Test
    fun signOut_shouldDeleteLocalData() = runTest(UnconfinedTestDispatcher()) {
        signerManager.signOut()

        coVerify { mockImageRepository.deleteStoredImages() }
        coVerify { mockEventRepository.deleteAll() }
        coVerify { mockEnrolmentRecordRepository.deleteAll() }
        coVerify { scannerManager.deleteFirmwareFiles() }
        coVerify { mockLicenseRepository.deleteCachedLicenses() }
    }

    @Test
    fun `when signedInProjectId is accessed then authStore is invoked`() {
        signerManager.signedInProjectId

        verify(exactly = 1) { mockAuthStore.signedInProjectId }
    }

    private suspend fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, token)

    private fun mockStoreCredentialsLocally() =
        every { mockAuthStore.signedInProjectId } returns (DEFAULT_PROJECT_ID)

    private fun mockRemoteSignedIn(error: Boolean = false) =
        coEvery { mockAuthStore.storeFirebaseToken(token) }.apply {
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
                    ProjectWithConfig(
                        Project(
                            DEFAULT_PROJECT_ID,
                            "local",
                            ProjectState.RUNNING,
                            "",
                            "",
                            "some_bucket_url",
                            "",
                            tokenizationKeys = emptyMap()
                        ),
                        ProjectConfiguration(
                            "id", DEFAULT_PROJECT_ID, "", mockk(), mockk(), mockk(), mockk(), mockk(), mockk(), null,
                        ),
                    )
                )
            } else {
                this.throws(Exception("Failed to fetch project info"))
            }
        }

}
