package com.simprints.infra.login.db

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.domain.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.network.exceptions.NetworkConnectionException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FirebaseManagerImpl @Inject constructor(
    private val loginInfoManager: LoginInfoManager,
    @ApplicationContext private val context: Context,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher
) : RemoteDbManager {

    override suspend fun signIn(token: Token) {
        cacheTokenClaims(token.value)
        cacheFirebaseOptions(token)
        val result = try {
            FirebaseAuth.getInstance(getCoreApp()).signInWithCustomToken(token.value).await()
        } catch (e: Exception) {
            throw transformFirebaseExceptionIfNeeded(e)
        }
        Simber.d("Signed in with: ${result.user?.uid}")
    }

    override fun signOut() {
        clearCachedTokenClaims()
        // On legacy projects they may not have a separate Core Firebase Project, so we try to
        // log out on both just in case.
        try {
            FirebaseAuth.getInstance(getLegacyAppFallback()).signOut()
        } catch (e: Exception) {
            throw transformFirebaseExceptionIfNeeded(e)
        }
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
        withContext(dispatcherIO) {
            // Projects that were signed in and then updated to 2021.2.0 need to check the
            // previous Firebase project until they login again.
            val result = try {
                FirebaseAuth.getInstance(getLegacyAppFallback())
                    .currentUser
                    ?.getIdToken(false)
                    ?.await()
            } catch (ex: Exception) {
                Simber.e(ex, "Failed to get access token")
                throw transformFirebaseExceptionIfNeeded(ex)
            }

            result?.token
                ?.also { cacheTokenClaims(it) }
                ?: throw RemoteDbNotSignedInException()
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

    private fun transformFirebaseExceptionIfNeeded(e: Exception): Exception {
        return when (e) {
            // Rethrow as NetworkConnectionException so we handle it properly as connectivity issue
            is FirebaseNetworkException, is ApiException -> NetworkConnectionException(cause = e)
            else -> e
        }
    }

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val TOKEN_USER_ID_CLAIM = "userId"

        private const val CORE_BACKEND_PROJECT = "coreBackendFirebaseProject"
    }
}
