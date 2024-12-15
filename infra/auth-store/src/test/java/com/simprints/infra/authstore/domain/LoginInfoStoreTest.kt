package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.security.SecurityManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LoginInfoStoreTest {
    @MockK
    private lateinit var ctx: Context

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var legacySharedPreferences: SharedPreferences

    @MockK
    private lateinit var legacyEditor: SharedPreferences.Editor

    @MockK
    private lateinit var secureSharedPreferences: SharedPreferences

    @MockK
    private lateinit var secureEditor: SharedPreferences.Editor

    private lateinit var loginInfoStoreImpl: LoginInfoStore

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        every { ctx.getSharedPreferences(any(), any()) } returns legacySharedPreferences
        every { legacySharedPreferences.edit() } returns legacyEditor

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns secureSharedPreferences
        every { secureSharedPreferences.edit() } returns secureEditor

        every { secureEditor.putString(any(), any()) } returns secureEditor
        every { secureEditor.putBoolean(any(), any()) } returns secureEditor

        loginInfoStoreImpl = LoginInfoStore(ctx, securityManager)
    }

    @Test
    fun `should migrate data from legacy prefs to secure prefs`() {
        every { legacySharedPreferences.contains(any()) } returns true
        every { legacySharedPreferences.getString(any(), any()) } returns "old-value"
        every { legacySharedPreferences.getBoolean(any(), any()) } returns true
        every { secureSharedPreferences.getString(any(), any()) } returns "new-value"

        val result = loginInfoStoreImpl.signedInProjectId

        verify {
            // check any amount of save data
            secureEditor.putString(any(), any())
            secureEditor.putBoolean(any(), any())
        }
        // Check that legacy prefs cleared
        verify(exactly = 7) { legacyEditor.remove(any()) }
        verify(exactly = 1) {
            legacyEditor.commit()
            secureEditor.commit()
        }
    }

    @Test
    fun `getting the signed in encrypted user id should returns it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "user"
        every { secureSharedPreferences.getBoolean(any(), any()) } returns true

        assertThat(loginInfoStoreImpl.signedInUserId).isEqualTo("user".asTokenizableEncrypted())
    }

    @Test
    fun `getting the signed in raw user id should return it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "user"
        every { secureSharedPreferences.getBoolean(any(), any()) } returns false

        assertThat(loginInfoStoreImpl.signedInUserId).isEqualTo("user".asTokenizableRaw())
    }

    @Test
    fun `setting the raw signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableRaw()

        verify(exactly = 1) {
            secureEditor.putString("USER_ID", "user")
            secureEditor.putBoolean("USER_ID_TOKENIZED", false)
        }
    }

    @Test
    fun `setting the tokenized signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableEncrypted()

        verify(exactly = 1) {
            secureEditor.putString("USER_ID", "user")
            secureEditor.putBoolean("USER_ID_TOKENIZED", true)
        }
    }

    @Test
    fun `setting the null to user id should clear it from the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = null

        verify { secureEditor.remove(any()) }
    }

    @Test
    fun `getting the signed in project id should returns it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "projectId"

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("projectId")
    }

    @Test
    fun `setting the signed in project id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInProjectId = "projectId"

        verify(exactly = 1) {
            secureEditor.putString("PROJECT_ID", "projectId")
            secureEditor.apply()
        }
    }

    @Test
    fun `getting the core firebase project id should returns it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase project id should returns an empty string if null`() {
        every { secureSharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("")
    }

    @Test
    fun `setting the core firebase project id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseProjectId = "firebase"

        verify(exactly = 1) {
            secureEditor.putString("CORE_FIREBASE_PROJECT_ID", "firebase")
            secureEditor.apply()
        }
    }

    @Test
    fun `getting the core firebase application id should returns it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase application id should returns an empty string if null`() {
        every { secureSharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("")
    }

    @Test
    fun `setting the core firebase application id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApplicationId = "firebase"

        verify(exactly = 1) {
            secureEditor.putString("CORE_FIREBASE_APPLICATION_ID", "firebase")
            secureEditor.apply()
        }
    }

    @Test
    fun `getting the core firebase api key should returns it`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "firebase"

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase api key should returns an empty string if null`() {
        every { secureSharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("")
    }

    @Test
    fun `setting the core firebase api key should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApiKey = "firebase"

        verify(exactly = 1) {
            secureEditor.putString("CORE_FIREBASE_API_KEY", "firebase")
            secureEditor.apply()
        }
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return an empty string if null`() {
        every { secureSharedPreferences.getString(any(), any()) } returns null

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("")
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return the signed in project id`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("project")
    }

    @Test
    fun `getting the project id claim should returns the string`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.projectIdTokenClaim).isEqualTo("project")
    }

    @Test
    fun `setting the project id claim should set in the shared preferences`() {
        loginInfoStoreImpl.projectIdTokenClaim = "project"

        verify(exactly = 1) {
            secureEditor.putString("PROJECT_ID_CLAIM", "project")
            secureEditor.apply()
        }
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is empty`() {
        every { secureSharedPreferences.getString(any(), any()) } returns ""

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is different`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "another project"

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is the same`() {
        every { secureSharedPreferences.getString(any(), any()) } returns "project"

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isTrue()
    }

    @Test
    fun `cleanCredentials should reset all the credentials`() {
        loginInfoStoreImpl.cleanCredentials()

        verify(exactly = 7) { secureEditor.remove(any()) }
        verify(exactly = 1) { secureEditor.commit() }
    }

    @Test
    fun `clearCachedTokenClaims should reset firebase claims`() {
        loginInfoStoreImpl.clearCachedTokenClaims()

        verify(exactly = 4) { secureEditor.remove(any()) }
    }
}
