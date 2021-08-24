package com.simprints.id.data.db.common

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.core.login.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.secure.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.id.secure.models.Token
import com.simprints.logging.Simber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

open class FirebaseManagerImpl(
    val loginInfoManager: LoginInfoManager,
    val context: Context
) : RemoteDbManager {

    override suspend fun signIn(token: Token) {
        cacheTokenClaims(token.value)
        cacheFirebaseOptions(token)
        val result =
            FirebaseAuth.getInstance(getCoreApp()).signInWithCustomToken(token.value).await()
        Simber.d("Signed in with: ${result.user?.uid}")
    }

    override fun signOut() {
        clearCachedTokenClaims()
        // On legacy projects they may not have a separate Core Firebase Project, so we try to
        // log out on both just in case.
        FirebaseAuth.getInstance(getLegacyAppFallback()).signOut()
        tryToDeleteCoreApp()
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
                FirebaseAuth.getInstance(getLegacyAppFallback()).getAccessToken(false).await()

            result.token?.let {
                cacheTokenClaims(it)
                it
            } ?: throw RemoteDbNotSignedInException()
        }


    private fun cacheTokenClaims(claim: String) {
        extractTokenPayloadAsJson(claim)?.let {
            if (it.has(TOKEN_PROJECT_ID_CLAIM)) {
                loginInfoManager.projectIdTokenClaim = it.getString(TOKEN_PROJECT_ID_CLAIM)
            }

            if (it.has(TOKEN_USER_ID_CLAIM)) {
                loginInfoManager.userIdTokenClaim = it.getString(TOKEN_USER_ID_CLAIM)
            }
        }
    }

    private fun cacheFirebaseOptions(token: Token) {
        loginInfoManager.coreFirebaseProjectId = token.projectId
        loginInfoManager.coreFirebaseApplicationId = token.applicationId
        loginInfoManager.coreFirebaseApiKey = token.apiKey
    }

    private fun getFirebaseOptions(token: Token): FirebaseOptions = FirebaseOptions.Builder()
        .setProjectId(token.projectId)
        .setApplicationId(token.applicationId)
        .setApiKey(token.apiKey)
        .build()

    private fun initializeCoreProject(token: Token, context: Context) {
        try {
            Firebase.initialize(
                context.applicationContext,
                getFirebaseOptions(token),
                CORE_BACKEND_PROJECT
            )
        } catch (ex: IllegalStateException) {
            // IllegalStateException = FirebaseApp name coreBackendFirebaseProject already exists!
            // We re-initialize because they might be signing into a different project.
            tryToDeleteCoreApp()
            Firebase.initialize(
                context.applicationContext,
                getFirebaseOptions(token),
                CORE_BACKEND_PROJECT
            )
        }
    }

    private fun tryToDeleteCoreApp() {
        try {
            getCoreApp().delete()
        } catch (ex: IllegalStateException) {
            Simber.d(ex)
        }
    }

    private fun clearCachedTokenClaims() {
        loginInfoManager.clearCachedTokenClaims()
    }

    override fun getCoreApp() = try {
        getCoreFirebaseApp()
    } catch (ex: IllegalStateException) {
        getCoreAppOrAttemptInit()
    }

    private fun getCoreFirebaseApp() = FirebaseApp.getInstance(CORE_BACKEND_PROJECT)

    @Synchronized
    private fun getCoreAppOrAttemptInit() = try {
        // We try to return the core app right away in case there are follow on synchronized requests
        getCoreFirebaseApp()
    } catch (ex: IllegalStateException) {
        val token = Token(
            "",
            loginInfoManager.coreFirebaseProjectId,
            loginInfoManager.coreFirebaseApiKey,
            loginInfoManager.coreFirebaseApplicationId
        )

        if (token.projectId.isEmpty() || token.apiKey.isEmpty() || token.applicationId.isEmpty())
            throw IllegalStateException("Core Firebase App options are not stored")

        initializeCoreProject(token, context)
        getCoreFirebaseApp()
    }

    override fun getLegacyAppFallback() = try {
        getCoreApp()
    } catch (ex: IllegalStateException) {
        // CORE_BACKEND_PROJECT doesn't exist
        FirebaseApp.getInstance()
    }

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val TOKEN_USER_ID_CLAIM = "userId"

        private const val CORE_BACKEND_PROJECT = "coreBackendFirebaseProject"
    }
}
