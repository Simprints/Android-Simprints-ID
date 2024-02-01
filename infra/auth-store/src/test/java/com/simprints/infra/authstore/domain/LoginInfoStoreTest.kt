package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LoginInfoStoreTest {

    private val ctx = mockk<Context>()
    private val sharedPreferences = mockk<SharedPreferences>()
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)

    private lateinit var loginInfoStoreImpl: LoginInfoStore

    @Before
    fun setup() {
        every { ctx.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        loginInfoStoreImpl = LoginInfoStore(ctx)
    }

    @Test
    fun `getting the signed in user id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "user"
        every { sharedPreferences.getBoolean(any(), any()) } returns true

        assertThat(loginInfoStoreImpl.signedInUserId).isEqualTo("user".asTokenizableRaw())
    }

    @Test
    fun `setting the raw signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableRaw()

        verify(exactly = 1) { editor.putString("USER_ID_VALUE", "user") }
        verify(exactly = 1) { editor.putBoolean("USER_ID_TOKENIZED", false) }
    }

    @Test
    fun `setting the tokenized signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableEncrypted()

        verify(exactly = 1) { editor.putString("USER_ID_VALUE", "user") }
        verify(exactly = 1) { editor.putBoolean("USER_ID_TOKENIZED", true) }
    }

    @Test
    fun `getting the signed in project id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "projectId"

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("projectId")
    }

    @Test
    fun `setting the signed in project id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInProjectId = "projectId"

        verify(exactly = 1) { editor.putString("PROJECT_ID", "projectId") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase project id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase project id should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("")
    }


    @Test
    fun `setting the core firebase project id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseProjectId = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_PROJECT_ID", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase application id should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase application id should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("")
    }


    @Test
    fun `setting the core firebase application id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApplicationId = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_APPLICATION_ID", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getting the core firebase api key should returns it`() {
        every { sharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase api key should returns an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("")
    }

    @Test
    fun `setting the core firebase api key should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApiKey = "firebase"

        verify(exactly = 1) { editor.putString("CORE_FIREBASE_API_KEY", "firebase") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return an empty string if null`() {
        every { sharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("")
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return the signed in project id`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("project")
    }

    @Test
    fun `getting the project id claim should returns the string`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.projectIdTokenClaim).isEqualTo("project")
    }

    @Test
    fun `setting the project id claim should set in the shared preferences`() {
        loginInfoStoreImpl.projectIdTokenClaim = "project"

        verify(exactly = 1) { editor.putString("PROJECT_ID_CLAIM", "project") }
        verify(exactly = 1) { editor.apply() }
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is empty`() {
        every { sharedPreferences.getString(any(), any()) } returns ""

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is different`() {
        every { sharedPreferences.getString(any(), any()) } returns "another project"

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is the same`() {
        every { sharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isTrue()
    }

    @Test
    fun `cleanCredentials should reset all the credentials`() {
        loginInfoStoreImpl.cleanCredentials()

        verify(exactly = 1) { editor.remove("USER_ID_VALUE") }
        verify(exactly = 1) { editor.remove("USER_ID_TOKENIZED") }
        verify(exactly = 1) { editor.putString("PROJECT_ID", "") }
        verify(exactly = 1) { editor.putString("ENCRYPTED_PROJECT_SECRET", "") }
        verify(exactly = 1) { editor.putString("PROJECT_ID_CLAIM", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_PROJECT_ID", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_APPLICATION_ID", "") }
        verify(exactly = 1) { editor.putString("CORE_FIREBASE_API_KEY", "") }
        verify(exactly = 7) { editor.apply() }
    }

    @Test
    fun `storeCredentials should set the credentials`() {
        loginInfoStoreImpl.storeCredentials("project")

        verify(exactly = 1) { editor.putString("PROJECT_ID", "project") }
        verify(exactly = 1) { editor.apply() }
    }
}
