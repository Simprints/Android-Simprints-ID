package com.simprints.infra.login.domain

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
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
    fun `getting the encrypted project secret should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "secret"

        assertThat(loginInfoManagerImpl.encryptedProjectSecret).isEqualTo("secret")
    }

    @Test
    fun `setting the encrypted project secret should set in the shared preferences`() {
        loginInfoManagerImpl.encryptedProjectSecret = "secret"

        verify(exactly = 1) { editor.putString("ENCRYPTED_PROJECT_SECRET", "secret") }
        verify(exactly = 1) { editor.apply() }
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

    @Test
    fun `getting the project id claim should returns the string`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoManagerImpl.projectIdTokenClaim).isEqualTo("project")
    }

    @Test
    fun `setting the project id claim should set in the shared preferences`() {
        loginInfoManagerImpl.projectIdTokenClaim = "project"

        verify(exactly = 1) { editor.putString("PROJECT_ID_CLAIM", "project") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the user id claim should returns the string`() {
        every { sharedPreferences.getString(any(), any()) } returns "user"

        assertThat(loginInfoManagerImpl.userIdTokenClaim).isEqualTo("user")
    }

    @Test
    fun `setting the user id claim should set in the shared preferences`() {
        loginInfoManagerImpl.userIdTokenClaim = "user"

        verify(exactly = 1) { editor.putString("USER_ID_CLAIM", "user") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is empty`() {
        every { sharedPreferences.getString(any(), any()) } returns ""

        assertThat(loginInfoManagerImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is different`() {
        every { sharedPreferences.getString(any(), any()) } returns "another project"

        assertThat(loginInfoManagerImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is the same`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoManagerImpl.isProjectIdSignedIn("project")).isTrue()
    }

    @Test
    fun `cleanCredentials should reset all the credentials`() {
        loginInfoManagerImpl.cleanCredentials()

        verify(exactly = 1) { editor.putString("PROJECT_ID", "") }
        verify(exactly = 1) { editor.putString("USER_ID", "") }
        verify(exactly = 1) { editor.putString("PROJECT_ID_CLAIM", "") }
        verify(exactly = 1) { editor.putString("USER_ID_CLAIM", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_PROJECT_ID", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_APPLICATION_ID", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_API_KEY", "") }
        verify(exactly = 7) { editor.apply() }
    }

    @Test
    fun `storeCredentials should set the credentials`() {
        loginInfoManagerImpl.storeCredentials("project", "user")

        verify(exactly = 1) { editor.putString("PROJECT_ID", "project") }
        verify(exactly = 1) { editor.putString("USER_ID", "user") }
        verify(exactly = 2) { editor.apply() }
    }
}
