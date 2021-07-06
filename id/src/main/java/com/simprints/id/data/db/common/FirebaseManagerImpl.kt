package com.simprints.id.data.db.common

import android.content.Context
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import com.simprints.core.login.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.secure.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.id.secure.models.Token
import com.simprints.id.tools.extensions.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

open class FirebaseManagerImpl(
    val loginInfoManager: LoginInfoManager,
    val context: Context
) : RemoteDbManager {

    override suspend fun signIn(token: Token) {
        cacheTokenClaims(token.value)
        initializeCoreProject(token)
        val result = getFirebaseAuth().signInWithCustomToken(token.value).awaitTask()
        Timber.d(result.user?.uid)
    }

    override fun signOut() {
        clearCachedTokenClaims()

        try {
            // On legacy project they may not have a separate Core Firebase Project, so we try to
            // log out on both just in case.
            getFirebaseAuth().signOut()
            FirebaseAuth.getInstance().signOut()
        } catch (ex: Exception) {
            Timber.d(ex)
        }
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
            val result = getFirebaseAuth().getAccessToken(false).awaitTask()
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

    private fun getFirebaseAuth() = FirebaseAuth.getInstance(Firebase.app(CORE_BACKEND_PROJECT))

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val TOKEN_USER_ID_CLAIM = "userId"

        const val CORE_BACKEND_PROJECT = "coreBackendFirebaseProject"
    }
}
