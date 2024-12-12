package com.simprints.infra.authstore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.db.FirebaseAuthManager
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.network.SimApiClientFactory
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthStoreImplTest {
    private val loginInfoStore = mockk<LoginInfoStore>(relaxed = true)
    private val firebaseAuthManager = mockk<FirebaseAuthManager>(relaxed = true)
    private val simApiClientFactory = mockk<SimApiClientFactory>(relaxed = true)

    private val loginManagerManagerImpl = AuthStoreImpl(
        loginInfoStore,
        firebaseAuthManager,
        simApiClientFactory,
    )

    @Test
    fun `get signedUserId should call the correct method`() {
        every { loginInfoStore.signedInUserId } returns USER_ID
        val receivedUserId = loginManagerManagerImpl.signedInUserId

        assertThat(receivedUserId).isEqualTo(USER_ID)
    }

    @Test
    fun `set signedUserId should call the correct method`() {
        every { loginInfoStore.signedInUserId } returns USER_ID
        loginManagerManagerImpl.signedInUserId = USER_ID

        verify { loginInfoStore.setProperty("signedInUserId").value(USER_ID) }
    }

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
    fun `getSignedInProjectIdOrEmpty should call the correct method`() {
        every { loginInfoStore.signedInProjectId } returns PROJECT_ID
        val receivedProjectId = loginManagerManagerImpl.signedInProjectId

        assertThat(receivedProjectId).isEqualTo(PROJECT_ID)
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
    fun `signIn should call the correct method`() = runTest(UnconfinedTestDispatcher()) {
        loginManagerManagerImpl.storeFirebaseToken(TOKEN)

        coVerify(exactly = 1) { firebaseAuthManager.signIn(TOKEN) }
    }

    @Test
    fun `signOut should call the correct method`() {
        loginManagerManagerImpl.clearFirebaseToken()

        verify(exactly = 1) { firebaseAuthManager.signOut() }
    }

    @Test
    fun `isSignedIn should call the correct method`() {
        every { firebaseAuthManager.isSignedIn(PROJECT_ID) } returns true
        val receivedIsSignedIn = loginManagerManagerImpl.isFirebaseSignedIn(PROJECT_ID)

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
        private const val PROJECT_ID = "projectId"
        private val USER_ID = "userId".asTokenizableRaw()
        private val TOKEN = Token("token", "projectId", "apiKey", "application")
        private val FIREBASE_APP = mockk<FirebaseApp>()
        private val SIM_API_CLIENT = mockk<SimNetwork.SimApiClient<SimRemoteInterface>>()
    }
}
