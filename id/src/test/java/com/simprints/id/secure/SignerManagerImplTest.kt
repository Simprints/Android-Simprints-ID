package com.simprints.id.secure

import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.secure.models.Token
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class SignerManagerImplTest {

    @MockK lateinit var mockProjectRepository: ProjectRepository
    @MockK lateinit var mockRemoteDbManager: RemoteDbManager
    @MockK lateinit var mockLoginInfoManager: LoginInfoManager
    @MockK lateinit var mockPreferencesManager: PreferencesManager
    @MockK lateinit var mockSyncManager: SyncManager
    @MockK lateinit var mockPeopleSyncManager: PeopleSyncManager
    @MockK lateinit var mockSecurityStateScheduler: SecurityStateScheduler

    private lateinit var signerManager: SignerManagerImpl

    private val token = Token("some_token")

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        signerManager = SignerManagerImpl(
            mockProjectRepository,
            mockRemoteDbManager,
            mockLoginInfoManager,
            mockPreferencesManager,
            mockPeopleSyncManager,
            mockSyncManager,
            mockSecurityStateScheduler
        )
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSignInToRemoteDb() = runBlockingTest {
        mockRemoteSignedIn()
        mockFetchingProjectInfo()

        signIn()

        coVerify { mockRemoteDbManager.signIn(token.value) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signInToRemoteFails_signInShouldFail() = runBlockingTest {
        mockRemoteSignedIn(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldStoreCredentialsLocally() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        verify { mockLoginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun storeCredentialsFails_signInShouldFail() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldFetchProjectInfo() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()

        signIn()

        coVerify { mockProjectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun loadAndRefreshCacheFails_signInShouldFail() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSucceed() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldScheduleSecurityStateCheck() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInfo()
        mockResumePeopleSync()

        signIn()

        verify { mockSecurityStateScheduler.scheduleSecurityStateCheck() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_shouldRemoveAnyState() = runBlockingTest {
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
    fun signOut_shouldCancelPeriodicSecurityStateCheck() = runBlockingTest {
        every { mockLoginInfoManager.signedInProjectId } returns DEFAULT_PROJECT_ID

        signerManager.signOut()

        verify { mockSecurityStateScheduler.cancelSecurityStateCheck() }
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
        coEvery { mockRemoteDbManager.signIn(token.value) }.apply {
            if(error) {
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
    private fun verifyStoredCredentialsGotCleaned() = verify { mockLoginInfoManager.cleanCredentials() }
    private fun verifyRemoteManagerGotSignedOut() = verify { mockRemoteDbManager.signOut() }
    private fun verifyLastSyncInfoGotDeleted() = coVerify { mockPeopleSyncManager.deleteSyncInfo() }
    private fun verifyAllSharedPreferencesExceptRealmKeysGotCleared() = verify { mockPreferencesManager.clearAllSharedPreferencesExceptRealmKeys() }
}
