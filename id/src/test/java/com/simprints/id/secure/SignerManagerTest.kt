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
import com.simprints.id.services.scheduledSync.subjects.master.SubjectsSyncManager
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class SignerManagerTest {

    @MockK lateinit var projectRepository: ProjectRepository
    @MockK lateinit var remoteDbManager: RemoteDbManager
    @MockK lateinit var loginInfoManager: LoginInfoManager
    @MockK lateinit var preferencesManager: PreferencesManager
    @MockK lateinit var syncManager: SyncManager
    @MockK lateinit var subjectsSyncManager: SubjectsSyncManager

    private lateinit var signerManager: SignerManagerImpl

    private val token = Token("some_token")

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        signerManager = SignerManagerImpl(
            projectRepository,
            remoteDbManager,
            loginInfoManager,
            preferencesManager,
            subjectsSyncManager,
            syncManager
        )

        mockkStatic("com.simprints.id.tools.extensions.PerformanceMonitoring_extKt")
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSignInToRemoteDb() = runBlockingTest {
        mockRemoteSignedIn()
        mockFetchingProjectInto()

        signIn()

        coVerify { remoteDbManager.signIn(token.value) }
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
        mockFetchingProjectInto()

        signIn()

        verify { loginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }
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
        mockFetchingProjectInto()

        signIn()

        coVerify { projectRepository.loadFromRemoteAndRefreshCache(DEFAULT_PROJECT_ID) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun loadAndRefreshCacheFails_signInShouldFail() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto(true)

        assertThrows<Throwable> { signIn() }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signIn_shouldSucceed() = runBlockingTest {
        mockRemoteSignedIn()
        mockStoreCredentialsLocally()
        mockFetchingProjectInto()
        mockResumePeopleSync()

        signIn()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun signOut_shouldRemoveAnyState() = runBlockingTest {
        every { loginInfoManager.signedInProjectId } returns DEFAULT_PROJECT_ID

        signerManager.signOut()

        verifyUpSyncGotPaused()
        verifyStoredCredentialsGotCleaned()
        verifyRemoteManagerGotSignedOut()
        verifyLastSyncInfoGotDeleted()
        verifyAllSharedPreferencesExceptRealmKeysGotCleared()
    }

    private suspend fun signIn() = signerManager.signIn(DEFAULT_PROJECT_ID, DEFAULT_USER_ID, token)

    private fun mockStoreCredentialsLocally(error: Boolean = false) =
        every { loginInfoManager.storeCredentials(DEFAULT_PROJECT_ID, DEFAULT_USER_ID) }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to store credentials"))
            }
        }

    private fun mockRemoteSignedIn(error: Boolean = false) =
        coEvery { remoteDbManager.signIn(token.value) }.apply {
            if(error) {
                this.throws(Throwable("Failed to store credentials"))
            } else {
                this.returns(Unit)
            }
        }

    private fun mockFetchingProjectInto(error: Boolean = false) =
        coEvery { projectRepository.loadFromRemoteAndRefreshCache(any()) }.apply {
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
        coEvery { syncManager.scheduleBackgroundSyncs() }.apply {
            if (!error) {
                this.returns(Unit)
            } else {
                this.throws(Throwable("Failed to resume upsync"))
            }
        }

    private fun verifyUpSyncGotPaused() = verify { syncManager.cancelBackgroundSyncs() }
    private fun verifyStoredCredentialsGotCleaned() = verify { loginInfoManager.cleanCredentials() }
    private fun verifyRemoteManagerGotSignedOut() = verify { remoteDbManager.signOut() }
    private fun verifyLastSyncInfoGotDeleted() = coVerify { subjectsSyncManager.deleteSyncInfo() }
    private fun verifyAllSharedPreferencesExceptRealmKeysGotCleared() = verify { preferencesManager.clearAllSharedPreferencesExceptRealmKeys() }
}
