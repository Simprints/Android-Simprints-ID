package com.simprints.id.data.db.common


import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.simprints.core.login.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FirebaseManagerImplTest {

    @MockK
    lateinit var loginInfoManager: LoginInfoManager

    @MockK
    lateinit var context: Context

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()
    private val testDispatcherProvider = TestDispatcherProvider(testCoroutineRule)
    private lateinit var firebaseManagerImpl: FirebaseManagerImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        firebaseManagerImpl = FirebaseManagerImpl(loginInfoManager, context, testDispatcherProvider)
    }


    @Test(expected = RemoteDbNotSignedInException::class)
    fun `test getCurrentToken throw RemoteDbNotSignedInException if FirebaseNoSignedInUserException`() =
        runBlocking {
            //Given
            val firebaseAuth: FirebaseAuth = mockk()
            every { firebaseAuth.getAccessToken(any()) } throws FirebaseNoSignedInUserException("")
            mockkStatic(FirebaseAuth::class)
            mockkStatic(FirebaseApp::class)
            every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
            every {  FirebaseApp.getInstance() } returns mockk()
            //When
            val result = firebaseManagerImpl.getCurrentToken()
            //Then it throws

        }

    @Test
    fun `test getCurrentToken success`() = runBlocking {
        //Given
        val firebaseAuth: FirebaseAuth = mockk()
        every {
            firebaseAuth.getAccessToken(any())
        } returns Tasks.forResult(GetTokenResult("Token",HashMap()))
        mockkStatic(FirebaseAuth::class)
        every { FirebaseAuth.getInstance(any()) } returns firebaseAuth
        every { FirebaseAuth.getInstance() } returns firebaseAuth
        mockkStatic(FirebaseApp::class)
        every {  FirebaseApp.getInstance() } returns mockk()

        //When
        val result = firebaseManagerImpl.getCurrentToken()
        //Then
        Truth.assertThat(result).isEqualTo("Token")

    }
}
