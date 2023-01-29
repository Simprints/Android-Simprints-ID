package com.simprints.infra.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.IntegrityTokenRequester
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginManagerImplTest {

    private val authenticationRemoteDataSource =
        mockk<AuthenticationRemoteDataSource>(relaxed = true)
    private val integrityTokenRequester = mockk<IntegrityTokenRequester>(relaxed = true)
    private val loginInfoManager = mockk<LoginInfoManager>(relaxed = true)
    private val remoteDbManager = mockk<RemoteDbManager>(relaxed = true)
    private val simApiClientFactory = mockk<SimApiClientFactory>(relaxed = true)

    private val loginManagerManagerImpl = LoginManagerImpl(
        authenticationRemoteDataSource,
        integrityTokenRequester,
        loginInfoManager,
        remoteDbManager,
        simApiClientFactory
    )

    @Test
    fun `get projectIdTokenClaim should call the correct method`() {
        every { loginInfoManager.projectIdTokenClaim } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.projectIdTokenClaim

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
    }

    @Test
    fun `set projectIdTokenClaim should call the correct method`() {
        every { loginInfoManager.projectIdTokenClaim } returns PROJECT_ID
        loginManagerManagerImpl.projectIdTokenClaim = PROJECT_ID

        verify { loginInfoManager.setProperty("projectIdTokenClaim").value(PROJECT_ID) }
    }

    @Test
    fun `get userIdTokenClaim should call the correct method`() {
        every { loginInfoManager.userIdTokenClaim } returns USER_ID
        val receivedProjectId = loginManagerManagerImpl.userIdTokenClaim

        assertThat(receivedProjectId).isEqualTo(USER_ID)
    }

    @Test
    fun `set userIdTokenClaim should call the correct method`() {
        every { loginInfoManager.userIdTokenClaim } returns USER_ID
        loginManagerManagerImpl.userIdTokenClaim = USER_ID

        verify { loginInfoManager.setProperty("userIdTokenClaim").value(USER_ID) }
    }

    @Test
    fun `get encryptedProjectSecret should call the correct method`() {
        every { loginInfoManager.encryptedProjectSecret } returns SECRET
        val receivedProjectId = loginManagerManagerImpl.encryptedProjectSecret

        assertThat(receivedProjectId).isEqualTo(SECRET)
    }

    @Test
    fun `set encryptedProjectSecret should call the correct method`() {
        every { loginInfoManager.encryptedProjectSecret } returns SECRET
        loginManagerManagerImpl.encryptedProjectSecret = SECRET

        verify { loginInfoManager.setProperty("encryptedProjectSecret").value(SECRET) }
    }

    @Test
    fun `get signedInProjectId should call the correct method`() {
        every { loginInfoManager.signedInProjectId } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.signedInProjectId

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
    }

    @Test
    fun `set signedInProjectId should call the correct method`() {
        every { loginInfoManager.signedInProjectId } returns PROJECT_ID
        loginManagerManagerImpl.signedInProjectId = PROJECT_ID

        verify { loginInfoManager.setProperty("signedInProjectId").value(PROJECT_ID) }
    }

    @Test
    fun `get signedInUserId should call the correct method`() {
        every { loginInfoManager.signedInUserId } returns USER_ID
        val receivedProjectId = loginManagerManagerImpl.signedInUserId

        assertThat(receivedProjectId).isEqualTo(USER_ID)
    }

    @Test
    fun `set signedInUserId should call the correct method`() {
        every { loginInfoManager.signedInUserId } returns USER_ID
        loginManagerManagerImpl.signedInUserId = USER_ID

        verify { loginInfoManager.setProperty("signedInUserId").value(USER_ID) }
    }

    @Test
    fun `get coreFirebaseProjectId should call the correct method`() {
        every { loginInfoManager.coreFirebaseProjectId } returns FIREBASE_PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.coreFirebaseProjectId

        assertThat(receivedProjectId).isEqualTo(FIREBASE_PROJECT_ID)
    }

    @Test
    fun `set coreFirebaseProjectId should call the correct method`() {
        every { loginInfoManager.coreFirebaseProjectId } returns FIREBASE_PROJECT_ID
        loginManagerManagerImpl.coreFirebaseProjectId = FIREBASE_PROJECT_ID

        verify { loginInfoManager.setProperty("coreFirebaseProjectId").value(FIREBASE_PROJECT_ID) }
    }

    @Test
    fun `get coreFirebaseApplicationId should call the correct method`() {
        every { loginInfoManager.coreFirebaseApplicationId } returns APPLICATION_ID
        val receivedProjectId = loginManagerManagerImpl.coreFirebaseApplicationId

        assertThat(receivedProjectId).isEqualTo(APPLICATION_ID)
    }

    @Test
    fun `set coreFirebaseApplicationId should call the correct method`() {
        every { loginInfoManager.coreFirebaseApplicationId } returns APPLICATION_ID
        loginManagerManagerImpl.coreFirebaseApplicationId = APPLICATION_ID

        verify { loginInfoManager.setProperty("coreFirebaseApplicationId").value(APPLICATION_ID) }
    }

    @Test
    fun `get coreFirebaseApiKey should call the correct method`() {
        every { loginInfoManager.coreFirebaseApiKey } returns API_KEY
        val receivedProjectId = loginManagerManagerImpl.coreFirebaseApiKey

        assertThat(receivedProjectId).isEqualTo(API_KEY)
    }

    @Test
    fun `set coreFirebaseApiKey should call the correct method`() {
        every { loginInfoManager.coreFirebaseApiKey } returns API_KEY
        loginManagerManagerImpl.coreFirebaseApiKey = API_KEY

        verify { loginInfoManager.setProperty("coreFirebaseApiKey").value(API_KEY) }
    }

    @Test
    fun `requestIntegrityToken should call the correct method`() {
        every { integrityTokenRequester.getToken(NONCE) } returns INTEGRITY_TOKEN
        val receivedToken = loginManagerManagerImpl.requestIntegrityToken(NONCE)

        assertThat(receivedToken).isEqualTo(INTEGRITY_TOKEN)
    }

    @Test
    fun `requestAuthenticationData should call the correct method`() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery {
            authenticationRemoteDataSource.requestAuthenticationData(
                PROJECT_ID,
                USER_ID,
                DEVICE_ID
            )
        } returns AUTHENTICATION_DATA
        val receivedAuthenticationData =
            loginManagerManagerImpl.requestAuthenticationData(PROJECT_ID, USER_ID, DEVICE_ID)

        assertThat(receivedAuthenticationData).isEqualTo(AUTHENTICATION_DATA)
    }

    @Test
    fun `requestAuthToken should call the correct method`() = runTest(
        UnconfinedTestDispatcher()
    ) {
        coEvery {
            authenticationRemoteDataSource.requestAuthToken(
                PROJECT_ID,
                USER_ID,
                AUTH_REQUEST
            )
        } returns TOKEN
        val receivedToken =
            loginManagerManagerImpl.requestAuthToken(PROJECT_ID, USER_ID, AUTH_REQUEST)

        assertThat(receivedToken).isEqualTo(TOKEN)
    }

    @Test
    fun `getEncryptedProjectSecretOrEmpty should call the correct method`() {
        every { loginInfoManager.getEncryptedProjectSecretOrEmpty() } returns SECRET
        val receivedSecret = loginManagerManagerImpl.getEncryptedProjectSecretOrEmpty()

        assertThat(receivedSecret).isEqualTo(SECRET)
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should call the correct method`() {
        every { loginInfoManager.getSignedInProjectIdOrEmpty() } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.getSignedInProjectIdOrEmpty()

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
    }

    @Test
    fun `getSignedInUserIdOrEmpty should call the correct method`() {
        every { loginInfoManager.getSignedInUserIdOrEmpty() } returns USER_ID
        val receivedUserId = loginManagerManagerImpl.getSignedInUserIdOrEmpty()

        assertThat(receivedUserId).isEqualTo(USER_ID)
    }

    @Test
    fun `isProjectIdSignedIn should call the correct method`() {
        every { loginInfoManager.isProjectIdSignedIn(PROJECT_ID) } returns true
        val receivedIsSignedIn = loginManagerManagerImpl.isProjectIdSignedIn(PROJECT_ID)

        assertThat(receivedIsSignedIn).isTrue()
    }

    @Test
    fun `cleanCredentials should call the correct method`() {
        loginManagerManagerImpl.cleanCredentials()

        verify(exactly = 1) { loginInfoManager.cleanCredentials() }
    }

    @Test
    fun `clearCachedTokenClaims should call the correct method`() {
        loginManagerManagerImpl.clearCachedTokenClaims()

        verify(exactly = 1) { loginInfoManager.clearCachedTokenClaims() }
    }

    @Test
    fun `storeCredentials should call the correct method`() {
        loginManagerManagerImpl.storeCredentials(PROJECT_ID, USER_ID)

        verify(exactly = 1) { loginInfoManager.storeCredentials(PROJECT_ID, USER_ID) }
    }

    @Test
    fun `signIn should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        loginManagerManagerImpl.signIn(TOKEN)

        coVerify(exactly = 1) { remoteDbManager.signIn(TOKEN) }
    }

    @Test
    fun `signOut should call the correct method`() {
        loginManagerManagerImpl.signOut()

        verify(exactly = 1) { remoteDbManager.signOut() }
    }

    @Test
    fun `isSignedIn should call the correct method`() {
        every { remoteDbManager.isSignedIn(PROJECT_ID, USER_ID) } returns true
        val receivedIsSignedIn = loginManagerManagerImpl.isSignedIn(PROJECT_ID, USER_ID)

        assertThat(receivedIsSignedIn).isTrue()
    }

    @Test
    fun `getCurrentToken should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { remoteDbManager.getCurrentToken() } returns TOKEN_STRING
        val receivedToken = loginManagerManagerImpl.getCurrentToken()

        assertThat(receivedToken).isEqualTo(TOKEN_STRING)
    }

    @Test
    fun `getCoreApp should call the correct method`() {
        every { remoteDbManager.getCoreApp() } returns FIREBASE_APP
        val receivedApp = loginManagerManagerImpl.getCoreApp()

        assertThat(receivedApp).isEqualTo(FIREBASE_APP)
    }

    @Test
    fun `getLegacyAppFallback should call the correct method`() {
        every { remoteDbManager.getLegacyAppFallback() } returns FIREBASE_APP
        val receivedApp = loginManagerManagerImpl.getLegacyAppFallback()

        assertThat(receivedApp).isEqualTo(FIREBASE_APP)
    }

    @Test
    fun `buildClient should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { simApiClientFactory.buildClient(SimRemoteInterface::class) } returns SIM_API_CLIENT
        val receivedClient = loginManagerManagerImpl.buildClient(SimRemoteInterface::class)

        assertThat(receivedClient).isEqualTo(SIM_API_CLIENT)
    }

    @Test
    fun `buildUnauthenticatedClient should call the correct method`() =
        runTest(UnconfinedTestDispatcher()) {
            coEvery { simApiClientFactory.buildUnauthenticatedClient(SimRemoteInterface::class) } returns SIM_API_CLIENT
            val receivedClient =
                loginManagerManagerImpl.buildUnauthenticatedClient(SimRemoteInterface::class)

            assertThat(receivedClient).isEqualTo(SIM_API_CLIENT)
        }

    companion object {
        private const val INTEGRITY_TOKEN = "token"
        private const val NONCE = "nonce"
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val USER_ID = "userId"
        private const val SECRET = "secret"
        private const val FIREBASE_PROJECT_ID = "project"
        private const val API_KEY = "apiKey"
        private const val APPLICATION_ID = "applicationId"
        private val AUTHENTICATION_DATA = AuthenticationData("public_key", "nonce")
        private val AUTH_REQUEST = AuthRequest("secret", "token", "deviceId")
        private val TOKEN = Token("token", "projectId", "apiKey", "application")
        private const val TOKEN_STRING = "token"
        private val FIREBASE_APP = mockk<FirebaseApp>()
        private val SIM_API_CLIENT = mockk<SimNetwork.SimApiClient<SimRemoteInterface>>()
    }
}
