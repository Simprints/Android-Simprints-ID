package com.simprints.infra.authstore.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LoginInfoStore @Inject constructor(
    @ApplicationContext ctx: Context,
    securityManager: SecurityManager,
) {
    companion object {
        private const val LEGACY_PREF_FILE_NAME = "b3f0cf9b-4f3f-4c5b-bf85-7b1f44eddd7a"
        private const val SECURE_PREF_FILE_NAME = "99caf5cd-7b1c-4127-912d-77d4c35c51f3"

        private const val USER_ID_VALUE: String = "USER_ID"
        private const val USER_ID_TOKENIZED: String = "USER_ID_TOKENIZED"
        private const val PROJECT_ID: String = "PROJECT_ID"
        private const val PROJECT_ID_CLAIM: String = "PROJECT_ID_CLAIM"
        private const val CORE_FIREBASE_PROJECT_ID = "CORE_FIREBASE_PROJECT_ID"
        private const val CORE_FIREBASE_APPLICATION_ID = "CORE_FIREBASE_APPLICATION_ID"
        private const val CORE_FIREBASE_API_KEY = "CORE_FIREBASE_API_KEY"
    }

    @Deprecated("Data has been migrated to secure prefs. Delete after there are no users below 2024.1.0")
    private val prefs: SharedPreferences =
        ctx.getSharedPreferences(LEGACY_PREF_FILE_NAME, Context.MODE_PRIVATE)

    private val securePrefs = securityManager.buildEncryptedSharedPreferences(SECURE_PREF_FILE_NAME)

    /**
     * Ensures that data has been migrated to secure prefs before accessing it.
     */
    private fun getSecurePrefs(): SharedPreferences {
        if (prefs.contains(PROJECT_ID)) {
            securePrefs.edit(commit = true) {
                putString(USER_ID_VALUE, prefs.getString(USER_ID_VALUE, ""))
                putBoolean(USER_ID_TOKENIZED, prefs.getBoolean(USER_ID_TOKENIZED, false))
                putString(PROJECT_ID, prefs.getString(PROJECT_ID, ""))
                putString(PROJECT_ID_CLAIM, prefs.getString(PROJECT_ID_CLAIM, ""))
                putString(CORE_FIREBASE_PROJECT_ID, prefs.getString(CORE_FIREBASE_PROJECT_ID, ""))
                putString(CORE_FIREBASE_APPLICATION_ID, prefs.getString(CORE_FIREBASE_APPLICATION_ID, ""))
                putString(CORE_FIREBASE_API_KEY, prefs.getString(CORE_FIREBASE_API_KEY, ""))
            }
            prefs.clearValues()
        }
        return securePrefs
    }

    var signedInUserId: TokenizableString? = null
        get() = getSecurePrefs().let {
            val value = it.getString(USER_ID_VALUE, null)
            when {
                value == null -> null
                it.getBoolean(USER_ID_TOKENIZED, false) -> TokenizableString.Tokenized(value)
                else -> TokenizableString.Raw(value)
            }
        }
        set(value) {
            field = value
            getSecurePrefs().edit {
                if (value == null) {
                    remove(USER_ID_VALUE)
                    remove(USER_ID_TOKENIZED)
                } else {
                    putBoolean(USER_ID_TOKENIZED, value.isTokenized())
                    putString(USER_ID_VALUE, value.value)
                }
            }
        }

    private val signedInProjectIdFlow: MutableStateFlow<String> = MutableStateFlow(
        getSecurePrefs().getString(PROJECT_ID, "").orEmpty(),
    )

    var signedInProjectId: String = ""
        get() = getSecurePrefs().getString(PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            getSecurePrefs().edit { putString(PROJECT_ID, field) }
            signedInProjectIdFlow.tryEmit(value)
        }

    fun observeSignedInProjectId(): StateFlow<String> = signedInProjectIdFlow.asStateFlow()

    // Core Firebase Project details. We store them to initialize the core Firebase project.
    var coreFirebaseProjectId: String = ""
        get() = securePrefs.getString(CORE_FIREBASE_PROJECT_ID, "").orEmpty()
        set(value) {
            field = value
            getSecurePrefs().edit { putString(CORE_FIREBASE_PROJECT_ID, field) }
        }

    var coreFirebaseApplicationId: String = ""
        get() = securePrefs.getString(CORE_FIREBASE_APPLICATION_ID, "").orEmpty()
        set(value) {
            field = value
            getSecurePrefs().edit { putString(CORE_FIREBASE_APPLICATION_ID, field) }
        }

    var coreFirebaseApiKey: String = ""
        get() = securePrefs.getString(CORE_FIREBASE_API_KEY, "").orEmpty()
        set(value) {
            field = value
            getSecurePrefs().edit { putString(CORE_FIREBASE_API_KEY, field) }
        }

    // Cached claims in the auth token. We used them to check whether the user is signed or not
    // in without reading the token from Firebase (async operation)
    var projectIdTokenClaim: String? = ""
        get() = securePrefs.getString(PROJECT_ID_CLAIM, "")
        set(value) {
            field = value
            getSecurePrefs().edit { putString(PROJECT_ID_CLAIM, field ?: "") }
        }

    fun isProjectIdSignedIn(possibleProjectId: String): Boolean = signedInProjectId.isNotEmpty() && signedInProjectId == possibleProjectId

    fun cleanCredentials() {
        securePrefs.clearValues()
        prefs.clearValues()
        signedInProjectIdFlow.tryEmit("")
    }

    fun clearCachedTokenClaims() {
        getSecurePrefs().edit {
            remove(PROJECT_ID_CLAIM)
            remove(CORE_FIREBASE_PROJECT_ID)
            remove(CORE_FIREBASE_APPLICATION_ID)
            remove(CORE_FIREBASE_API_KEY)
        }
    }

    private fun SharedPreferences.clearValues() = edit(commit = true) {
        remove(USER_ID_VALUE)
        remove(USER_ID_TOKENIZED)
        remove(PROJECT_ID)
        remove(PROJECT_ID_CLAIM)
        remove(CORE_FIREBASE_PROJECT_ID)
        remove(CORE_FIREBASE_APPLICATION_ID)
        remove(CORE_FIREBASE_API_KEY)
    }
}
