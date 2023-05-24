package com.simprints.infra.login

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.simprints.infra.login.db.FirebaseAuthManager
import com.simprints.infra.login.domain.IntegrityTokenRequester
import com.simprints.infra.login.domain.LoginInfoStore
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
    private val loginInfoStore = mockk<LoginInfoStore>(relaxed = true)
    private val firebaseAuthManager = mockk<FirebaseAuthManager>(relaxed = true)
    private val simApiClientFactory = mockk<SimApiClientFactory>(relaxed = true)

    private val loginManagerManagerImpl = LoginManagerImpl(
        authenticationRemoteDataSource,
        integrityTokenRequester,
        loginInfoStore,
        firebaseAuthManager,
        simApiClientFactory
    )

    @Test
    fun `get signedInProjectId should call the correct method`() {
        every { loginInfoStore.signedInProjectId } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.signedInProjectId

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
    }

    @Test
    fun `set signedInProjectId should call the correct method`() {
        every { loginInfoStore.signedInProjectId } returns PROJECT_ID
        loginManagerManagerImpl.signedInProjectId = PROJECT_ID

        verify { loginInfoStore.setProperty("signedInProjectId").value(PROJECT_ID) }
    }

    @Test
    fun `get signedInUserId should call the correct method`() {
        every { loginInfoStore.signedInUserId } returns USER_ID
        val receivedProjectId = loginManagerManagerImpl.signedInUserId

        assertThat(receivedProjectId).isEqualTo(USER_ID)
    }

    @Test
    fun `set signedInUserId should call the correct method`() {
        every { loginInfoStore.signedInUserId } returns USER_ID
        loginManagerManagerImpl.signedInUserId = USER_ID

        verify { loginInfoStore.setProperty("signedInUserId").value(USER_ID) }
    }

    @Test
    fun `requestIntegrityToken should call the correct method`() = runTest {
        coEvery { integrityTokenRequester.getToken(NONCE) } returns INTEGRITY_TOKEN
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
    fun `getSignedInProjectIdOrEmpty should call the correct method`() {
        every { loginInfoStore.signedInProjectId } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.signedInProjectId

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
    }

    @Test
    fun `getSignedInUserIdOrEmpty should call the correct method`() {
        every { loginInfoStore.signedInUserId } returns USER_ID
        val receivedUserId = loginManagerManagerImpl.signedInUserId

        assertThat(receivedUserId).isEqualTo(USER_ID)
    }

    @Test
    fun `isProjectIdSignedIn should call the correct method`() {
        every { loginInfoStore.isProjectIdSignedIn(PROJECT_ID) } returns true
        val receivedIsSignedIn = loginManagerManagerImpl.isProjectIdSignedIn(PROJECT_ID)

        assertThat(receivedIsSignedIn).isTrue()
    }

    @Test
    fun `cleanCredentials should call the correct method`() {
        loginManagerManagerImpl.cleanCredentials()

        verify(exactly = 1) { loginInfoStore.cleanCredentials() }
    }

    @Test
    fun `storeCredentials should call the correct method`() {
        loginManagerManagerImpl.storeCredentials(PROJECT_ID, USER_ID)

        verify(exactly = 1) { loginInfoStore.storeCredentials(PROJECT_ID, USER_ID) }
    }

    @Test
    fun `signIn should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        loginManagerManagerImpl.signIn(TOKEN)

        coVerify(exactly = 1) { firebaseAuthManager.signIn(TOKEN) }
    }

    @Test
    fun `signOut should call the correct method`() {
        loginManagerManagerImpl.signOut()

        verify(exactly = 1) { firebaseAuthManager.signOut() }
    }

    @Test
    fun `isSignedIn should call the correct method`() {
        every { firebaseAuthManager.isSignedIn(PROJECT_ID, USER_ID) } returns true
        val receivedIsSignedIn = loginManagerManagerImpl.isSignedIn(PROJECT_ID, USER_ID)

        assertThat(receivedIsSignedIn).isTrue()
    }

    @Test
    fun `getCoreApp should call the correct method`() {
        every { firebaseAuthManager.getCoreApp() } returns FIREBASE_APP
        val receivedApp = loginManagerManagerImpl.getCoreApp()

        assertThat(receivedApp).isEqualTo(FIREBASE_APP)
    }

    @Test
    fun `getLegacyAppFallback should call the correct method`() {
        every { firebaseAuthManager.getLegacyAppFallback() } returns FIREBASE_APP
        val receivedApp = loginManagerManagerImpl.getLegacyAppFallback()

        assertThat(receivedApp).isEqualTo(FIREBASE_APP)
    }

    @Test
    fun `buildClient should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        coEvery { simApiClientFactory.buildClient(SimRemoteInterface::class) } returns SIM_API_CLIENT
        val receivedClient = loginManagerManagerImpl.buildClient(SimRemoteInterface::class)

        assertThat(receivedClient).isEqualTo(SIM_API_CLIENT)
    }

    companion object {
        private const val INTEGRITY_TOKEN = "token"
        private const val NONCE = "nonce"
        private const val PROJECT_ID = "projectId"
        private const val DEVICE_ID = "deviceId"
        private const val USER_ID = "userId"
        private const val FIREBASE_PROJECT_ID = "project"
        private val AUTHENTICATION_DATA = AuthenticationData("public_key", "nonce")
        private val AUTH_REQUEST = AuthRequest("secret", "token", "deviceId")
        private val TOKEN = Token("token", "projectId", "apiKey", "application")
        private val FIREBASE_APP = mockk<FirebaseApp>()
        private val SIM_API_CLIENT = mockk<SimNetwork.SimApiClient<SimRemoteInterface>>()
    }
}
