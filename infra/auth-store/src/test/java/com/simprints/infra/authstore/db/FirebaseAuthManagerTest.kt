package com.simprints.infra.authstore.db

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthManagerTest {
    companion object {
        private const val GCP_PROJECT_ID = "GCP_PROJECT_ID"
        private const val API_KEY = "API_KEY"
        private const val APPLICATION_ID = "APPLICATION_ID"
    }

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseApp = mockk<FirebaseApp>(relaxed = true)
    private val firebaseUser = mockk<FirebaseUser>(relaxed = true)
    private val loginInfoStore = mockk<LoginInfoStore>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val firebaseOptionsBuilder = mockk<FirebaseOptions.Builder>(relaxed = true)
    private val firebaseAuthManager = FirebaseAuthManager(loginInfoStore, context, UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        mockkStatic(Firebase::class)
        mockkStatic(FirebaseOptions.Builder::class)
        every { FirebaseApp.getInstance(any()) } returns firebaseApp
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
        every { firebaseOptionsBuilder.setApiKey(any()) } returns firebaseOptionsBuilder
        every { firebaseOptionsBuilder.setProjectId(any()) } returns firebaseOptionsBuilder
        every { firebaseOptionsBuilder.setApplicationId(any()) } returns firebaseOptionsBuilder
    }

    @Test
    fun `signIn should throw a NetworkConnectionException if Firebase throws FirebaseNetworkException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signInWithCustomToken(any()) } throws FirebaseNetworkException("Failed")

            assertThrows<NetworkConnectionException> {
                firebaseAuthManager.signIn(mockk(relaxed = true))
            }
        }

    @Test
    fun `signIn should throw a NetworkConnectionException if Firebase throws ApiException`() = runTest(UnconfinedTestDispatcher()) {
        every { firebaseAuth.signInWithCustomToken(any()) } throws ApiException(Status.RESULT_TIMEOUT)

        assertThrows<NetworkConnectionException> {
            firebaseAuthManager.signIn(mockk(relaxed = true))
        }
    }

    @Test
    fun `signOut should succeed`() = runTest(UnconfinedTestDispatcher()) {
        firebaseAuthManager.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
        verify(exactly = 1) { firebaseApp.delete() }
        verify(exactly = 1) { loginInfoStore.clearCachedTokenClaims() }
    }

    @Test
    fun `signOut should throw a NetworkConnectionException if Firebase throws FirebaseNetworkException`() =
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.signOut() } throws FirebaseNetworkException("Failed")

            assertThrows<NetworkConnectionException> {
                firebaseAuthManager.signOut()
            }
        }

    @Test
    fun `signOut should throw a NetworkConnectionException if Firebase throws ApiException`() = runTest(UnconfinedTestDispatcher()) {
        every { firebaseAuth.signOut() } throws ApiException(Status.RESULT_TIMEOUT)

        assertThrows<NetworkConnectionException> {
            firebaseAuthManager.signOut()
        }
    }

    @Test
    fun `isSignedIn should return true if the project id claim is null`() {
        every { loginInfoStore.projectIdTokenClaim } returns null
        assertThat(firebaseAuthManager.isSignedIn("")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is the same as the project id`() {
        every { loginInfoStore.projectIdTokenClaim } returns "project id"
        assertThat(firebaseAuthManager.isSignedIn("project id")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is not the same as the project id`() {
        every { loginInfoStore.projectIdTokenClaim } returns "project id"
        assertThat(firebaseAuthManager.isSignedIn("another project id")).isFalse()
    }

    @Test
    fun `getCurrentToken throws NetworkConnectionException if Firebase throws FirebaseNetworkException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.currentUser } returns firebaseUser
            every { firebaseUser.getIdToken(any()) } throws FirebaseNetworkException("Failed")
            assertThrows<NetworkConnectionException> { firebaseAuthManager.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken throws NetworkConnectionException if Firebase throws ApiException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.currentUser } returns firebaseUser
            every { firebaseUser.getIdToken(any()) } throws ApiException(Status.RESULT_TIMEOUT)
            assertThrows<NetworkConnectionException> { firebaseAuthManager.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken throws RemoteDbNotSignedInException if FirebaseNoSignedInUserException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.currentUser } returns null
            assertThrows<RemoteDbNotSignedInException> { firebaseAuthManager.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken success`() = runTest {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.getIdToken(any()) } returns Tasks.forResult(GetTokenResult("Token", HashMap()))

        val result = firebaseAuthManager.getCurrentToken()
        assertThat(result).isEqualTo("Token")
    }

    @Test
    fun `getCoreApp should init the app if the Firebase getInstance() throws an IllegalStateException`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoStore.coreFirebaseProjectId } returns GCP_PROJECT_ID
        every { loginInfoStore.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoStore.coreFirebaseApiKey } returns API_KEY
        every { Firebase.initialize(any(), any(), any()) } returns mockk()

        firebaseAuthManager.getCoreApp()

        verify(exactly = 1) {
            Firebase.initialize(
                any(),
                match {
                    it.apiKey == API_KEY && it.applicationId == APPLICATION_ID && it.projectId == GCP_PROJECT_ID
                },
                any(),
            )
        }
    }

    @Test
    fun `getCoreApp should throw an IllegalStateException the app if the Firebase getInstance() throws an IllegalStateException and the coreFirebaseProjectId is empty`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoStore.coreFirebaseProjectId } returns ""
        every { loginInfoStore.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoStore.coreFirebaseApiKey } returns API_KEY
        every { Firebase.initialize(any(), any(), any()) } returns mockk()

        assertThrows<IllegalStateException> { firebaseAuthManager.getCoreApp() }
    }

    @Test
    fun `getCoreApp should init the app and recreate the app if the Firebase getInstance() throws an IllegalStateException and the initialization failed`() {
        every { FirebaseApp.getInstance(any()) } throws IllegalStateException() andThenThrows IllegalStateException() andThen firebaseApp
        every { loginInfoStore.coreFirebaseProjectId } returns GCP_PROJECT_ID
        every { loginInfoStore.coreFirebaseApplicationId } returns APPLICATION_ID
        every { loginInfoStore.coreFirebaseApiKey } returns API_KEY
        every {
            Firebase.initialize(
                any(),
                any(),
                any(),
            )
        } throws IllegalStateException() andThen mockk<FirebaseApp>()

        firebaseAuthManager.getCoreApp()

        verify(exactly = 1) { firebaseApp.delete() }
        verify(exactly = 2) {
            Firebase.initialize(
                any(),
                match {
                    it.apiKey == API_KEY && it.applicationId == APPLICATION_ID && it.projectId == GCP_PROJECT_ID
                },
                any(),
            )
        }
    }
}
