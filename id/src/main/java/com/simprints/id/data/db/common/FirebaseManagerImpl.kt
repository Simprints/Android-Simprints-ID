package com.simprints.id.data.db.common

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import com.simprints.id.secure.JwtTokenHelper.Companion.extractTokenPayloadAsJson
import com.simprints.id.tools.extensions.awaitTask
import io.reactivex.Completable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

open class FirebaseManagerImpl(val loginInfoManager: LoginInfoManager) : RemoteDbManager {

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun signIn(token: String): Completable =
        Completable.fromAction {
            cacheTokenClaims(token)
            val result = Tasks.await(firebaseAuth.signInWithCustomToken(token))
            Timber.d(result.user.uid)
        }

    override fun signOut() {
        clearCachedTokenClaims()
        firebaseAuth.signOut()
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
            val result = firebaseAuth.getAccessToken(false).awaitTask()
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

    private fun clearCachedTokenClaims() {
        loginInfoManager.clearCachedTokenClaims()
    }

    companion object {
        private const val TOKEN_PROJECT_ID_CLAIM = "projectId"
        private const val TOKEN_USER_ID_CLAIM = "userId"
    }
}
