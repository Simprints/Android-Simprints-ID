package com.simprints.infra.login.db

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.exceptions.RemoteDbNotSignedInException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirebaseManagerImplTest {

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseApp = mockk<FirebaseApp>(relaxed = true)
    private val loginInfoManager = mockk<LoginInfoManager>(relaxed = true)
    private val context = mockk<Context>()
    private val firebaseManagerImpl = FirebaseManagerImpl(loginInfoManager, context)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getInstance(any()) } returns firebaseApp
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
    }

    @Test
    fun `signOut should succeed`() = runTest(UnconfinedTestDispatcher()) {
        firebaseManagerImpl.signOut()

        verify(exactly = 1) { firebaseAuth.signOut() }
        verify(exactly = 1) { firebaseApp.delete() }
        verify(exactly = 1) { loginInfoManager.clearCachedTokenClaims() }
    }

    @Test
    fun `isSignedIn should return true if the project id claim is null`() {
        every { loginInfoManager.projectIdTokenClaim } returns null
        assertThat(firebaseManagerImpl.isSignedIn("", "")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is the same as the project id`() {
        every { loginInfoManager.projectIdTokenClaim } returns "project id"
        assertThat(firebaseManagerImpl.isSignedIn("project id", "")).isTrue()
    }

    @Test
    fun `isSignedIn should return true if the project id claim is not the same as the project id`() {
        every { loginInfoManager.projectIdTokenClaim } returns "project id"
        assertThat(firebaseManagerImpl.isSignedIn("another project id", "")).isFalse()
    }

    @Test
    fun `getCurrentToken throw RemoteDbNotSignedInException if FirebaseNoSignedInUserException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.getAccessToken(any()) } throws FirebaseNoSignedInUserException("")
            assertThrows<RemoteDbNotSignedInException> { firebaseManagerImpl.getCurrentToken() }
        }
    }

    @Test
    fun `getCurrentToken success`() = runBlocking {
        every {
            firebaseAuth.getAccessToken(any())
        } returns Tasks.forResult(GetTokenResult("Token", HashMap()))

        val result = firebaseManagerImpl.getCurrentToken()
        assertThat(result).isEqualTo("Token")
    }
}
