package com.simprints.infra.login.db

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.models.Token
import io.mockk.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirebaseManagerImplIntegrationTest {

    companion object {
        private const val GCP_PROJECT_ID = "GCP_PROJECT_ID"
        private const val API_KEY = "API_KEY"
        private const val APPLICATION_ID = "APPLICATION_ID"
        private const val TOKEN_VALUE =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJvamVjdElkIjoicHJvamVjdCIsInVzZXJJZCI6InVzZXIifQ.ORqU6eJ-OHpSb1fY0hdzg_oGwTzxk08h_Ueo9ep48n0"
    }

    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val firebaseApp = mockk<FirebaseApp>(relaxed = true)
    private val loginInfoManager = mockk<LoginInfoManager>(relaxed = true)
    private val context = mockk<Context>()
    private val firebaseManagerImpl = FirebaseManagerImpl(loginInfoManager, context, UnconfinedTestDispatcher())

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
                relaxed = true
            )
        )
        val token = Token(TOKEN_VALUE, GCP_PROJECT_ID, API_KEY, APPLICATION_ID)
        firebaseManagerImpl.signIn(token)

        verify(exactly = 1) { firebaseAuth.signInWithCustomToken(TOKEN_VALUE) }
        verify(exactly = 1) { loginInfoManager.projectIdTokenClaim = "project" }
        verify(exactly = 1) { loginInfoManager.userIdTokenClaim = "user" }
        verify(exactly = 1) { loginInfoManager.coreFirebaseProjectId = GCP_PROJECT_ID }
        verify(exactly = 1) { loginInfoManager.coreFirebaseApplicationId = APPLICATION_ID }
        verify(exactly = 1) { loginInfoManager.coreFirebaseApiKey = API_KEY }
    }
}
