package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class LoginInfoStore @Inject constructor(
    @ApplicationContext ctx: Context,
) {

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

    // TODO Move data to an encrypted version - CORE-2590
    private val prefs: SharedPreferences = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    var signedInProjectId: String = ""
        get() = prefs.getString(PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(PROJECT_ID, field).apply()
        }

    var signedInUserId: String = ""
        get() = prefs.getString(USER_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(USER_ID, field).apply()
        }

    // Core Firebase Project details. We store them to initialize the core Firebase project.
    var coreFirebaseProjectId: String = ""
        get() = prefs.getString(CORE_FIREBASE_PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_PROJECT_ID, field).apply()
        }

    var coreFirebaseApplicationId: String = ""
        get() = prefs.getString(CORE_FIREBASE_APPLICATION_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_APPLICATION_ID, field).apply()
        }

    var coreFirebaseApiKey: String = ""
        get() = prefs.getString(CORE_FIREBASE_API_KEY, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(CORE_FIREBASE_API_KEY, field).apply()
        }

    // Cached claims in the auth token. We used them to check whether the user is signed or not
    // in without reading the token from Firebase (async operation)
    var projectIdTokenClaim: String? = ""
        get() = prefs.getString(PROJECT_ID_CLAIM, "")
        set(value) {
            field = value
            prefs.edit().putString(PROJECT_ID_CLAIM, field ?: "").apply()
        }

    var userIdTokenClaim: String? = ""
        get() = prefs.getString(USER_ID_CLAIM, "")
        set(value) {
            field = value
            prefs.edit().putString(USER_ID_CLAIM, field ?: "").apply()
        }

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        signedInProjectId.isNotEmpty() && signedInProjectId == possibleProjectId

    fun cleanCredentials() {
        signedInProjectId = ""
        signedInUserId = ""
        prefs.edit().putString(ENCRYPTED_PROJECT_SECRET, "").apply()

        clearCachedTokenClaims()
    }

    fun clearCachedTokenClaims() {
        projectIdTokenClaim = ""
        userIdTokenClaim = ""
        coreFirebaseProjectId = ""
        coreFirebaseApplicationId = ""
        coreFirebaseApiKey = ""
    }

    fun storeCredentials(projectId: String, userId: String) {
        signedInProjectId = projectId
        signedInUserId = userId
    }
}
