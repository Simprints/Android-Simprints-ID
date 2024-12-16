package com.simprints.infra.authstore.db

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.domain.models.Token
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseAuthManagerIntegrationTest {
    companion object {
        private const val GCP_PROJECT_ID = "GCP_PROJECT_ID"
        private const val API_KEY = "API_KEY"
        private const val APPLICATION_ID = "APPLICATION_ID"
        private const val TOKEN_VALUE =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJvamVjdElkIjoicHJvamVjdCIsInVzZXJJZCI6InVzZXIifQ.ORqU6eJ-OHpSb1fY0hdzg_oGwTzxk08h_Ueo9ep48n0"
    }

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseApp = mockk<FirebaseApp>(relaxed = true)
    private val loginInfoStore = mockk<LoginInfoStore>(relaxed = true)
    private val context = mockk<Context>()
    private val firebaseAuthManager = FirebaseAuthManager(loginInfoStore, context, UnconfinedTestDispatcher())

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getInstance(any()) } returns firebaseApp
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
    }

    @Test
    fun `signIn should succeed`() = runTest(UnconfinedTestDispatcher()) {
        every { firebaseAuth.signInWithCustomToken(TOKEN_VALUE) } returns Tasks.forResult(
            mockk<AuthResult>(
                relaxed = true,
            ),
        )
        val token = Token(TOKEN_VALUE, GCP_PROJECT_ID, API_KEY, APPLICATION_ID)
        firebaseAuthManager.signIn(token)

        verify(exactly = 1) { firebaseAuth.signInWithCustomToken(TOKEN_VALUE) }
        verify(exactly = 1) { loginInfoStore.projectIdTokenClaim = "project" }
        verify(exactly = 1) { loginInfoStore.coreFirebaseProjectId = GCP_PROJECT_ID }
        verify(exactly = 1) { loginInfoStore.coreFirebaseApplicationId = APPLICATION_ID }
        verify(exactly = 1) { loginInfoStore.coreFirebaseApiKey = API_KEY }
    }
}
