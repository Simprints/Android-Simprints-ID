package com.simprints.infra.authstore.db

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.simprints.core.DispatcherIO
import com.simprints.infra.authstore.domain.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.exceptions.NetworkConnectionException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class FirebaseAuthManager @Inject constructor(
    private val loginInfoStore: LoginInfoStore,
    @ApplicationContext private val context: Context,
    @DispatcherIO private val dispatcherIO: CoroutineDispatcher,
) {
    suspend fun signIn(token: Token) {
        cacheTokenClaims(token.value)
        cacheFirebaseOptions(token)
        val result = try {
            FirebaseAuth.getInstance(getCoreApp()).signInWithCustomToken(token.value).await()
        } catch (e: Exception) {
            throw transformFirebaseExceptionIfNeeded(e)
        }
        Simber.d("Signed in with: ${result.user?.uid}")
    }

    fun signOut() {
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

    fun isSignedIn(projectId: String): Boolean {
        val lastProjectIdClaim = loginInfoStore.projectIdTokenClaim
        return if (!lastProjectIdClaim.isNullOrEmpty()) {
            lastProjectIdClaim == projectId
        } else {
            // If projectId claim is not in the shared prefs, it's because the user
            // updated the app from a version that didn't have the claim cache built.
            // In this case we assume that the token is valid.
            true
        }
    }

    suspend fun getCurrentToken(): String = withContext(dispatcherIO) {
        // Projects that were signed in and then updated to 2021.2.0 need to check the
        // previous Firebase project until they login again.
        val result = try {
            FirebaseAuth
                .getInstance(getLegacyAppFallback())
                .currentUser
                ?.getIdToken(false)
                ?.await()
        } catch (ex: Exception) {
            Simber.e("Failed to get access token", ex, tag = LOGIN)
            throw transformFirebaseExceptionIfNeeded(ex)
        }

        result
            ?.token
            ?.also { cacheTokenClaims(it) }
            ?: throw RemoteDbNotSignedInException()
    }

    private fun cacheTokenClaims(claim: String) {
        extractTokenPayloadAsJson(claim)?.let {
            if (it.has(TOKEN_PROJECT_ID_CLAIM)) {
                loginInfoStore.projectIdTokenClaim = it.getString(TOKEN_PROJECT_ID_CLAIM)
            }
        }
    }

    private fun cacheFirebaseOptions(token: Token) {
        loginInfoStore.coreFirebaseProjectId = token.projectId
        loginInfoStore.coreFirebaseApplicationId = token.applicationId
        loginInfoStore.coreFirebaseApiKey = token.apiKey
    }

    private fun getFirebaseOptions(token: Token): FirebaseOptions = FirebaseOptions
        .Builder()
        .setProjectId(token.projectId)
        .setApplicationId(token.applicationId)
        .setApiKey(token.apiKey)
        .build()

    private fun initializeCoreProject(
        token: Token,
        context: Context,
    ) {
        try {
            Firebase.initialize(
                context.applicationContext,
                getFirebaseOptions(token),
                CORE_BACKEND_PROJECT,
            )
        } catch (ex: IllegalStateException) {
            // IllegalStateException = FirebaseApp name coreBackendFirebaseProject already exists!
            // We re-initialize because they might be signing into a different project.
            tryToDeleteCoreApp()
            Firebase.initialize(
                context.applicationContext,
                getFirebaseOptions(token),
                CORE_BACKEND_PROJECT,
            )
        }
    }

    private fun tryToDeleteCoreApp() {
        try {
            getCoreApp().delete()
        } catch (ex: IllegalStateException) {
            Simber.e("Failed to delete core app", ex, tag = LOGIN)
        }
    }

    private fun clearCachedTokenClaims() {
        loginInfoStore.clearCachedTokenClaims()
    }

    /**
     * Get the FirebaseApp that corresponds with the core backend. This FirebaseApp is only
     * initialized once the client has logged in.
     * @see signIn
     * @return FirebaseApp
     * @throws IllegalStateException if not initialized
     */
    fun getCoreApp() = try {
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
            loginInfoStore.coreFirebaseProjectId,
            loginInfoStore.coreFirebaseApiKey,
            loginInfoStore.coreFirebaseApplicationId,
        )
        check(!(token.projectId.isEmpty() || token.apiKey.isEmpty() || token.applicationId.isEmpty())) {
            "Core Firebase App options are not stored"
        }

        initializeCoreProject(token, context)
        getCoreFirebaseApp()
    }

    @Deprecated(
        message = "Since 2021.2.0. Can be removed once all projects are on 2021.2.0+",
        replaceWith = ReplaceWith("getCoreApp()"),
    )
    fun getLegacyAppFallback() = try {
        getCoreApp()
    } catch (ex: IllegalStateException) {
        // CORE_BACKEND_PROJECT doesn't exist
        FirebaseApp.getInstance()
    }

    private fun transformFirebaseExceptionIfNeeded(e: Exception): Exception = when (e) {
        // Rethrow as NetworkConnectionException so we handle it properly as connectivity issue
        is FirebaseNetworkException, is ApiException -> NetworkConnectionException(cause = e)
        else -> e
    }

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val CORE_BACKEND_PROJECT = "coreBackendFirebaseProject"
    }
}
