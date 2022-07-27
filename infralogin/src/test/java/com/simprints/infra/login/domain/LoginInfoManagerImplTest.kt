package com.simprints.infra.login.domain

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.login.exceptions.CredentialMissingException
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LoginInfoManagerImplTest {

    private val ctx = mockk<Context>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    private lateinit var loginInfoManagerImpl: LoginInfoManager

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        loginInfoManagerImpl = LoginInfoManagerImpl(ctx)
    }

    @Test
    fun `getting the signed in project id should throw an error if it's missing`() {
        every { sharedPreferences.getString(any(), any()) } returns ""

        assertThrows<CredentialMissingException> {
            loginInfoManagerImpl.signedInProjectId
        }
    }

    @Test
    fun `getting the signed in project id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "projectId"

        assertThat(loginInfoManagerImpl.signedInProjectId).isEqualTo("projectId")
    }

    @Test
    fun `setting the signed in project id should set in the shared preferences`() {
        loginInfoManagerImpl.signedInProjectId = "projectId"

        verify(exactly = 1) { editor.putString("PROJECT_ID", "projectId") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the signed in user id should throw an error if it's missing`() {
        every { sharedPreferences.getString(any(), any()) } returns ""

        assertThrows<CredentialMissingException> {
            loginInfoManagerImpl.signedInUserId
        }
    }

    @Test
    fun `getting the signed in user id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "userId"

        assertThat(loginInfoManagerImpl.signedInUserId).isEqualTo("userId")
    }

    @Test
    fun `setting the signed in user id should set in the shared preferences`() {
        loginInfoManagerImpl.signedInUserId = "userId"

        verify(exactly = 1) { editor.putString("USER_ID", "userId") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase project id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoManagerImpl.coreFirebaseProjectId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase project id should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoManagerImpl.coreFirebaseProjectId).isEqualTo("")
    }


    @Test
    fun `setting the core firebase project id should set in the shared preferences`() {
        loginInfoManagerImpl.coreFirebaseProjectId = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_PROJECT_ID", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase application id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoManagerImpl.coreFirebaseApplicationId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase application id should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoManagerImpl.coreFirebaseApplicationId).isEqualTo("")
    }


    @Test
    fun `setting the core firebase application id should set in the shared preferences`() {
        loginInfoManagerImpl.coreFirebaseApplicationId = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_APPLICATION_ID", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase api key should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoManagerImpl.coreFirebaseApiKey).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase api key should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoManagerImpl.coreFirebaseApiKey).isEqualTo("")
    }

    @Test
    fun `setting the core firebase api key should set in the shared preferences`() {
        loginInfoManagerImpl.coreFirebaseApiKey = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_API_KEY", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoManagerImpl.getSignedInProjectIdOrEmpty()).isEqualTo("")
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return the signed in project id`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoManagerImpl.getSignedInProjectIdOrEmpty()).isEqualTo("project")
    }

    @Test
    fun `getSignedInUserIdOrEmpty should return an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoManagerImpl.getSignedInUserIdOrEmpty()).isEqualTo("")
    }

    @Test
    fun `getSignedInUserIdOrEmpty should return the signed in project id`() {
        every { sharedPreferences.getString(any(), any()) } returns "user"

        assertThat(loginInfoManagerImpl.getSignedInUserIdOrEmpty()).isEqualTo("user")
    }
}
