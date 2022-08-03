package com.simprints.infra.login.domain

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

internal class LoginInfoManagerImpl @Inject constructor(ctx: Context) : LoginInfoManager {

    companion object {
        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE
        private const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
        private const val PROJECT_ID: String = "PROJECT_ID"
        private const val PROJECT_ID_CLAIM: String = "PROJECT_ID_CLAIM"
        private const val USER_ID_CLAIM: String = "USER_ID_CLAIM"
        private const val USER_ID: String = "USER_ID"
        private const val CORE_FIREBASE_PROJECT_ID = "CORE_FIREBASE_PROJECT_ID"
        private const val CORE_FIREBASE_APPLICATION_ID = "CORE_FIREBASE_APPLICATION_ID"
        private const val CORE_FIREBASE_API_KEY = "CORE_FIREBASE_API_KEY"
    }

    private val prefs: SharedPreferences = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    override var encryptedProjectSecret: String = ""
        get() = prefs.getString(ENCRYPTED_PROJECT_SECRET, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(ENCRYPTED_PROJECT_SECRET, field).apply()
        }

    override var signedInProjectId: String = ""
        get() = prefs.getString(PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(PROJECT_ID, field).apply()
        }

    override var signedInUserId: String = ""
        get() = prefs.getString(USER_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(USER_ID, field).apply()
        }

    override var coreFirebaseProjectId: String = ""
        get() = prefs.getString(CORE_FIREBASE_PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_PROJECT_ID, field).apply()
        }

    override var coreFirebaseApplicationId: String = ""
        get() = prefs.getString(CORE_FIREBASE_APPLICATION_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_APPLICATION_ID, field).apply()
        }

    override var coreFirebaseApiKey: String = ""
        get() = prefs.getString(CORE_FIREBASE_API_KEY, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_API_KEY, field).apply()
        }

    override var projectIdTokenClaim: String? = ""
        get() = prefs.getString(PROJECT_ID_CLAIM, "")
        set(value) {
            field = value
            prefs.edit().putString(PROJECT_ID_CLAIM, field ?: "").apply()
        }

    override var userIdTokenClaim: String? = ""
        get() = prefs.getString(USER_ID_CLAIM, "")
        set(value) {
            field = value
            prefs.edit().putString(USER_ID_CLAIM, field ?: "").apply()
        }

    override fun getEncryptedProjectSecretOrEmpty(): String =
        encryptedProjectSecret

    override fun getSignedInProjectIdOrEmpty(): String =
        signedInProjectId

    override fun getSignedInUserIdOrEmpty(): String =
        signedInUserId

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        getSignedInProjectIdOrEmpty().isNotEmpty() && getSignedInProjectIdOrEmpty() == possibleProjectId

    override fun cleanCredentials() {
        signedInProjectId = ""
        signedInUserId = ""
        clearCachedTokenClaims()
    }

    override fun clearCachedTokenClaims() {
        projectIdTokenClaim = ""
        userIdTokenClaim = ""
        coreFirebaseProjectId = ""
        coreFirebaseApplicationId = ""
        coreFirebaseApiKey = ""
    }

    override fun storeCredentials(projectId: String, userId: String) {
        signedInProjectId = projectId
        signedInUserId = userId
    }
}
