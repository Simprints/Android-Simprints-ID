package com.simprints.id.data.db.common

import com.auth0.jwt.JWT
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.exceptions.unexpected.RemoteDbNotSignedInException
import io.reactivex.Completable
import io.reactivex.Single
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

    override fun getCurrentToken(): Single<String> = Single.fromCallable {
        val result = Tasks.await(firebaseAuth.getAccessToken(false))
        result.token?.let {
            cacheTokenClaims(it)
            it
        } ?: throw RemoteDbNotSignedInException()
    }

    private fun cacheTokenClaims(token: String) {
        val claims = JWT.decode(token).claims
        loginInfoManager.projectIdTokenClaim = claims[projectIdClaim]?.asString()
        loginInfoManager.userIdTokenClaim = claims[userIdClaim]?.asString()
    }

    private fun clearCachedTokenClaims() {
        loginInfoManager.clearCachedTokenClaims()
    }

    companion object {
        private const val projectIdClaim = "projectId"
        private const val userIdClaim = "userId"
        const val RETRY_ATTEMPTS_FOR_NETWORK_CALLS = 5L
    }
}
