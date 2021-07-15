package com.simprints.id.data.db.common

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.core.login.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.secure.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.id.secure.models.Token
import com.simprints.id.tools.extensions.awaitTask
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class FirebaseManagerImpl(
    val loginInfoManager: LoginInfoManager,
    val context: Context
) : RemoteDbManager {

    override suspend fun signIn(token: Token) {
        cacheTokenClaims(token.value)
        initializeCoreProject(token)
        val result =
            FirebaseAuth.getInstance(getCoreApp()).signInWithCustomToken(token.value).awaitTask()
        Simber.d("Signed in with: ${result.user?.uid}")
    }

    override fun signOut() {
        clearCachedTokenClaims()
        // On legacy projects they may not have a separate Core Firebase Project, so we try to
        // log out on both just in case.
        FirebaseAuth.getInstance(getLegacyAppFallback()).signOut()
    }

    override fun isSignedIn(projectId: String, userId: String): Boolean {
        val lastProjectIdClaim = loginInfoManager.projectIdTokenClaim
        return if (!lastProjectIdClaim.isNullOrEmpty()) {
            lastProjectIdClaim == projectId
        } else {
            // If projectId claim is not in the shared prefs, it's because the user
            // updated the app from a version that didn't have the claim cache built.
            // In this case we assume that the token is valid.
            true
        }
    }

    override suspend fun getCurrentToken(): String =
        withContext(Dispatchers.IO) {
            // Projects that were signed in and then updated to 2021.2.0 need to check the
            // previous Firebase project until they login again.
            val result =
                FirebaseAuth.getInstance(getLegacyAppFallback()).getAccessToken(false).awaitTask()

            result.token?.let {
                cacheTokenClaims(it)
                it
            } ?: throw RemoteDbNotSignedInException()
        }


    private fun cacheTokenClaims(token: String) {
        extractTokenPayloadAsJson(token)?.let {
            if (it.has(TOKEN_PROJECT_ID_CLAIM)) {
                loginInfoManager.projectIdTokenClaim = it.getString(TOKEN_PROJECT_ID_CLAIM)
            }

            if (it.has(TOKEN_USER_ID_CLAIM)) {
                loginInfoManager.userIdTokenClaim = it.getString(TOKEN_USER_ID_CLAIM)
            }
        }
    }

    private fun getFirebaseOptions(token: Token): FirebaseOptions = FirebaseOptions.Builder()
        .setProjectId(token.projectId)
        .setApplicationId(token.applicationId)
        .setApiKey(token.apiKey)
        .build()

    private fun initializeCoreProject(token: Token) =
        Firebase.initialize(context, getFirebaseOptions(token), CORE_BACKEND_PROJECT)

    private fun clearCachedTokenClaims() {
        loginInfoManager.clearCachedTokenClaims()
    }

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val TOKEN_USER_ID_CLAIM = "userId"

        private const val CORE_BACKEND_PROJECT = "coreBackendFirebaseProject"

        /**
         * Get the FirebaseApp that corresponds with the core backend. This FirebaseApp is only
         * initialized once the client has logged in.
         * @see signIn
         * @return FirebaseApp
         * @throws IllegalStateException if not initialized
         */
        fun getCoreApp() = FirebaseApp.getInstance(CORE_BACKEND_PROJECT)

        @Deprecated(
            message = "Since 2021.2.0. Can be removed once all projects are on 2021.2.0+",
            replaceWith = ReplaceWith("getCoreApp()")
        )
        fun getLegacyAppFallback() = try {
            getCoreApp()
        } catch (ex: IllegalStateException) {
            // CORE_BACKEND_PROJECT doesn't exist
            FirebaseApp.getInstance()
        }

    }
}
