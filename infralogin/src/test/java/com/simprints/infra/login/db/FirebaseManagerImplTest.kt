package com.simprints.infra.login.db

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.exceptions.RemoteDbNotSignedInException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FirebaseManagerImplTest {

    private val firebaseAuth = mockk<FirebaseAuth>()
    private val loginInfoManager = mockk<LoginInfoManager>()
    private val context = mockk<Context>()
    private val firebaseManagerImpl = FirebaseManagerImpl(loginInfoManager, context)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(FirebaseAuth::class)
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getInstance(any()) } returns mockk()
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
    }


    @Test
    fun `test getCurrentToken throw RemoteDbNotSignedInException if FirebaseNoSignedInUserException`() {
        runTest(UnconfinedTestDispatcher()) {
            every { firebaseAuth.getAccessToken(any()) } throws FirebaseNoSignedInUserException("")
            assertThrows<RemoteDbNotSignedInException> { firebaseManagerImpl.getCurrentToken() }
        }
    }

    @Test
    fun `test getCurrentToken success`() = runBlocking {
        every {
            firebaseAuth.getAccessToken(any())
        } returns Tasks.forResult(GetTokenResult("Token", HashMap()))

        val result = firebaseManagerImpl.getCurrentToken()
        assertThat(result).isEqualTo("Token")
    }
}
