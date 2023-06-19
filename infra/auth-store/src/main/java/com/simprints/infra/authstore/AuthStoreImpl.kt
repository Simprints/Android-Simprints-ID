package com.simprints.infra.authstore

import com.google.firebase.FirebaseApp
import com.simprints.infra.authstore.db.FirebaseAuthManager
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.network.SimApiClientFactory
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass


internal class AuthStoreImpl @Inject constructor(
    private val loginInfoStore: LoginInfoStore,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val simApiClientFactory: SimApiClientFactory,
) : AuthStore {

    override var signedInProjectId: String
        get() = loginInfoStore.signedInProjectId
        set(value) {
            loginInfoStore.signedInProjectId = value
        }
    override var signedInUserId: String
        get() = loginInfoStore.signedInUserId
        set(value) {
            loginInfoStore.signedInUserId = value
        }

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        loginInfoStore.isProjectIdSignedIn(possibleProjectId)

    override fun cleanCredentials() {
        loginInfoStore.cleanCredentials()
    }

    override fun storeCredentials(projectId: String, userId: String) {
        loginInfoStore.storeCredentials(projectId, userId)
    }

    override suspend fun storeFirebaseToken(token: Token) {
        firebaseAuthManager.signIn(token)
    }

    override fun clearFirebaseToken() {
        firebaseAuthManager.signOut()
    }

    override fun isSignedIn(projectId: String, userId: String): Boolean =
        firebaseAuthManager.isSignedIn(projectId, userId)

    override fun getCoreApp(): FirebaseApp =
        firebaseAuthManager.getCoreApp()

    override fun getLegacyAppFallback(): FirebaseApp =
        firebaseAuthManager.getLegacyAppFallback()

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> =
        simApiClientFactory.buildClient(remoteInterface)

}