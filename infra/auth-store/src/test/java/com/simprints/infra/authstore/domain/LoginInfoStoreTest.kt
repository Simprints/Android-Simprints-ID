package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.*
import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.CORE_FIREBASE_API_KEY
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.CORE_FIREBASE_APPLICATION_ID
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.CORE_FIREBASE_PROJECT_ID
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.LEGACY_PREF_FILE_NAME
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.PROJECT_ID
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.PROJECT_ID_CLAIM
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.SECURE_PREF_FILE_NAME
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.USER_ID_TOKENIZED
import com.simprints.infra.authstore.domain.LoginInfoStore.Companion.USER_ID_VALUE
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LoginInfoStoreTest {
    @SpyK
    private lateinit var ctx: Context

    @MockK
    private lateinit var securityManager: SecurityManager

    private lateinit var legacySharedPreferences: SharedPreferences

    private lateinit var legacyEditor: SharedPreferences.Editor

    private lateinit var secureSharedPreferences: SharedPreferences

    private lateinit var secureEditor: SharedPreferences.Editor

    private lateinit var loginInfoStoreImpl: LoginInfoStore

    @Before
    fun setup() {
        ctx = ApplicationProvider.getApplicationContext()
        secureSharedPreferences = ctx.getSharedPreferences(SECURE_PREF_FILE_NAME, Context.MODE_PRIVATE)
        secureEditor = secureSharedPreferences.edit()
        legacySharedPreferences = ctx.getSharedPreferences(LEGACY_PREF_FILE_NAME, Context.MODE_PRIVATE)
        legacyEditor = legacySharedPreferences.edit()
        MockKAnnotations.init(this)

        every { ctx.getSharedPreferences(LEGACY_PREF_FILE_NAME, Context.MODE_PRIVATE) } returns legacySharedPreferences
        every { securityManager.buildEncryptedSharedPreferences(SECURE_PREF_FILE_NAME) } returns secureSharedPreferences

        loginInfoStoreImpl = LoginInfoStore(ctx, securityManager)
    }

    @Test
    fun `should migrate data from legacy prefs to secure prefs`() {
        val projectId = "project-id"
        legacyEditor.putString(PROJECT_ID, projectId).apply()

        val result = loginInfoStoreImpl.signedInProjectId

        assertThat(result).isEqualTo(projectId)
        assertThat(legacySharedPreferences.all).isEmpty()
    }

    @Test
    fun `getting the signed in encrypted user id should returns it`() {
        secureEditor.putString(USER_ID_VALUE, "user").apply()
        secureEditor.putBoolean(USER_ID_TOKENIZED, true).apply()

        assertThat(loginInfoStoreImpl.signedInUserId).isEqualTo("user".asTokenizableEncrypted())
    }

    @Test
    fun `getting the signed in raw user id should return it`() {
        secureEditor.putString(USER_ID_VALUE, "user").apply()
        secureEditor.putBoolean(USER_ID_TOKENIZED, false).apply()

        assertThat(loginInfoStoreImpl.signedInUserId).isEqualTo("user".asTokenizableRaw())
    }

    @Test
    fun `setting the raw signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableRaw()
        assertThat(secureSharedPreferences.getString(USER_ID_VALUE, null)).isEqualTo("user")
        assertThat(secureSharedPreferences.getBoolean(USER_ID_TOKENIZED, true)).isFalse()
    }

    @Test
    fun `setting the tokenized signed in user id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = "user".asTokenizableEncrypted()

        assertThat(secureSharedPreferences.getString(USER_ID_VALUE, null)).isEqualTo("user")
        assertThat(secureSharedPreferences.getBoolean(USER_ID_TOKENIZED, false)).isTrue()
    }

    @Test
    fun `setting the null to user id should clear it from the shared preferences`() {
        loginInfoStoreImpl.signedInUserId = null

        assertThat(secureSharedPreferences.all).doesNotContainKey(USER_ID_VALUE)
    }

    @Test
    fun `getting the signed in project id should returns it`() {
        secureEditor.putString(PROJECT_ID, "projectId").apply()
        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("projectId")
    }

    @Test
    fun `setting the signed in project id should set in the shared preferences`() {
        loginInfoStoreImpl.signedInProjectId = "projectId"

        assertThat(secureSharedPreferences.getString(PROJECT_ID, null)).isEqualTo("projectId")
    }

    @Test
    fun `getting the core firebase project id should returns it`() {
        secureEditor.putString(CORE_FIREBASE_PROJECT_ID, "firebase").apply()

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase project id should returns an empty string if null`() {
        secureEditor.putString(CORE_FIREBASE_PROJECT_ID, null).apply()

        assertThat(loginInfoStoreImpl.coreFirebaseProjectId).isEqualTo("")
    }

    @Test
    fun `setting the core firebase project id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseProjectId = "firebase"
        assertThat(secureSharedPreferences.getString(CORE_FIREBASE_PROJECT_ID, null)).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase application id should returns it`() {
        secureEditor.putString(CORE_FIREBASE_APPLICATION_ID, "firebase").apply()
        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase application id should returns an empty string if null`() {
        secureEditor.putString(CORE_FIREBASE_APPLICATION_ID, null).apply()

        assertThat(loginInfoStoreImpl.coreFirebaseApplicationId).isEqualTo("")
    }

    @Test
    fun `setting the core firebase application id should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApplicationId = "firebase"

        assertThat(secureSharedPreferences.getString(CORE_FIREBASE_APPLICATION_ID, null)).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase api key should returns it`() {
        secureEditor.putString(CORE_FIREBASE_API_KEY, "firebase").apply()

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("firebase")
    }

    @Test
    fun `getting the core firebase api key should returns an empty string if null`() {
        secureEditor.putString(CORE_FIREBASE_API_KEY, null).apply()

        assertThat(loginInfoStoreImpl.coreFirebaseApiKey).isEqualTo("")
    }

    @Test
    fun `setting the core firebase api key should set in the shared preferences`() {
        loginInfoStoreImpl.coreFirebaseApiKey = "firebase"

        assertThat(secureSharedPreferences.getString(CORE_FIREBASE_API_KEY, null)).isEqualTo("firebase")
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return an empty string if null`() {
        secureEditor.putString(PROJECT_ID, null).apply()
        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("")
    }

    @Test
    fun `getSignedInProjectIdOrEmpty should return the signed in project id`() {
        secureEditor.putString(PROJECT_ID, "project").apply()

        assertThat(loginInfoStoreImpl.signedInProjectId).isEqualTo("project")
    }

    @Test
    fun `getting the project id claim should returns the string`() {
        secureEditor.putString(PROJECT_ID_CLAIM, "project").apply()
        assertThat(loginInfoStoreImpl.projectIdTokenClaim).isEqualTo("project")
    }

    @Test
    fun `setting the project id claim should set in the shared preferences`() {
        loginInfoStoreImpl.projectIdTokenClaim = "project"
        assertThat(secureSharedPreferences.getString(PROJECT_ID_CLAIM, null)).isEqualTo("project")
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is empty`() {
        secureEditor.putString(PROJECT_ID, "").apply()
        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is different`() {
        secureEditor.putString(PROJECT_ID, "another project").apply()

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isFalse()
    }

    @Test
    fun `isProjectIdSignedIn should return false if the signed in project id is the same`() {
        secureEditor.putString(PROJECT_ID, "project").apply()

        assertThat(loginInfoStoreImpl.isProjectIdSignedIn("project")).isTrue()
    }

    @Test
    fun `cleanCredentials should reset all the credentials`() {
        secureEditor.putString(PROJECT_ID_CLAIM, "project").apply()
        secureEditor.putString(CORE_FIREBASE_PROJECT_ID, "project").apply()
        secureEditor.putString(CORE_FIREBASE_APPLICATION_ID, "project").apply()

        loginInfoStoreImpl.cleanCredentials()

        assertThat(secureSharedPreferences.all).isEmpty()
    }

    @Test
    fun `clearCachedTokenClaims should reset firebase claims`() {
        secureEditor.putString(PROJECT_ID_CLAIM, "project").apply()
        secureEditor.putString(CORE_FIREBASE_PROJECT_ID, "project").apply()
        secureEditor.putString(CORE_FIREBASE_APPLICATION_ID, "project").apply()
        loginInfoStoreImpl.clearCachedTokenClaims()

        assertThat(secureSharedPreferences.getString(CORE_FIREBASE_PROJECT_ID, null)).isNull()
        assertThat(secureSharedPreferences.getString(CORE_FIREBASE_APPLICATION_ID, null)).isNull()
        assertThat(secureSharedPreferences.getString(PROJECT_ID_CLAIM, null)).isNull()
    }

    @Test
    fun `observeSignedInProjectId should return flow with initial project id value`() = runTest {
        loginInfoStoreImpl.signedInProjectId = "initial-project-id"

        val flow = loginInfoStoreImpl.observeSignedInProjectId()
        val initialValue = flow.first()

        assertThat(initialValue).isEqualTo("initial-project-id")
    }

    @Test
    fun `observeSignedInProjectId should return flow with empty string when project id is empty`() = runTest {
        loginInfoStoreImpl.signedInProjectId = ""

        val flow = loginInfoStoreImpl.observeSignedInProjectId()
        val initialValue = flow.first()

        assertThat(initialValue).isEqualTo("")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeSignedInProjectId should emit new values when signedInProjectId is updated`() = runTest {
        val flow = loginInfoStoreImpl.observeSignedInProjectId()
        val emissionList = mutableListOf<String>()
        val job = launch {
            flow.collect {
                emissionList.add(it)
            }
        }
        loginInfoStoreImpl.signedInProjectId = "initial-project-id"
        advanceUntilIdle()
        loginInfoStoreImpl.signedInProjectId = "updated-project-id"
        advanceUntilIdle()
        assertThat(emissionList).containsExactly("initial-project-id", "updated-project-id").inOrder()
        job.cancel()
    }

    @Test
    fun `observeSignedInProjectId should emit empty string when credentials are cleared`() = runTest {
        loginInfoStoreImpl.signedInProjectId = "project-id"
        val flow = loginInfoStoreImpl.observeSignedInProjectId()
        val initialValue = flow.first()

        assertThat(initialValue).isEqualTo("project-id")

        loginInfoStoreImpl.cleanCredentials()

        val clearedValue = flow.first()
        assertThat(clearedValue).isEqualTo("")
    }
}
