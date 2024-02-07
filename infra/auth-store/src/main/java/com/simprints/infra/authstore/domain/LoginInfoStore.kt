package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.simprints.core.domain.tokenization.TokenizableString
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Array.setBoolean
import javax.inject.Inject

internal class LoginInfoStore @Inject constructor(
    @ApplicationContext ctx: Context,
) {

    companion object {

        private const val PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val PREF_MODE = Context.MODE_PRIVATE
        private const val ENCRYPTED_PROJECT_SECRET: String = "ENCRYPTED_PROJECT_SECRET"
        private const val USER_ID_VALUE: String = "USER_ID"
        private const val USER_ID_TOKENIZED: String = "USER_ID_TOKENIZED"
        private const val PROJECT_ID: String = "PROJECT_ID"
        private const val PROJECT_ID_CLAIM: String = "PROJECT_ID_CLAIM"
        private const val CORE_FIREBASE_PROJECT_ID = "CORE_FIREBASE_PROJECT_ID"
        private const val CORE_FIREBASE_APPLICATION_ID = "CORE_FIREBASE_APPLICATION_ID"
        private const val CORE_FIREBASE_API_KEY = "CORE_FIREBASE_API_KEY"
    }

    // TODO Move data to an encrypted version - CORE-2590
    private val prefs: SharedPreferences = ctx.getSharedPreferences(PREF_FILE_NAME, PREF_MODE)

    var signedInUserId: TokenizableString? = null
        get() {
            val value = prefs.getString(USER_ID_VALUE, null)
            return when {
                value == null -> null
                prefs.getBoolean(USER_ID_TOKENIZED, false) -> TokenizableString.Tokenized(value)
                else -> TokenizableString.Raw(value)
            }
        }
        set(value) = prefs.edit {
            if (value == null) {
                remove(USER_ID_VALUE)
                remove(USER_ID_TOKENIZED)
            } else {
                putBoolean(USER_ID_TOKENIZED, value is TokenizableString.Tokenized)
                putString(USER_ID_VALUE, value.value)
            }
            field = value
        }

    var signedInProjectId: String = ""
        get() = prefs.getString(PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            prefs.edit().putString(PROJECT_ID, field).apply()
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

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        signedInProjectId.isNotEmpty() && signedInProjectId == possibleProjectId

    fun cleanCredentials() {
        signedInUserId = null
        signedInProjectId = ""
        prefs.edit().putString(ENCRYPTED_PROJECT_SECRET, "").apply()

        clearCachedTokenClaims()
    }

    fun clearCachedTokenClaims() {
        projectIdTokenClaim = ""
        coreFirebaseProjectId = ""
        coreFirebaseApplicationId = ""
        coreFirebaseApiKey = ""
    }

    fun storeCredentials(projectId: String) {
        signedInProjectId = projectId
    }
}
