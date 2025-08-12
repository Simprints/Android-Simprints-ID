package com.simprints.infra.authstore

import com.google.firebase.FirebaseApp
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.db.FirebaseAuthManager
import com.simprints.infra.authstore.domain.LoginInfoStore
import com.simprints.infra.authstore.domain.models.Token
import com.simprints.infra.authstore.network.SimApiClientFactory
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.reflect.KClass

internal class AuthStoreImpl @Inject constructor(
    private val loginInfoStore: LoginInfoStore,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val simApiClientFactory: SimApiClientFactory,
) : AuthStore {
    override var signedInUserId: TokenizableString?
        get() = loginInfoStore.signedInUserId
        set(value) {
            loginInfoStore.signedInUserId = value
        }

    override var signedInProjectId: String
        get() = loginInfoStore.signedInProjectId
        set(value) {
            loginInfoStore.signedInProjectId = value
        }

    override fun observeSignedInProjectId(): StateFlow<String> = loginInfoStore.observeSignedInProjectId()

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean = loginInfoStore.isProjectIdSignedIn(possibleProjectId)

    override fun cleanCredentials() {
        loginInfoStore.cleanCredentials()
    }

    override suspend fun storeFirebaseToken(token: Token) {
        firebaseAuthManager.signIn(token)
    }

    override fun clearFirebaseToken() {
        firebaseAuthManager.signOut()
    }

    override fun isFirebaseSignedIn(projectId: String): Boolean = firebaseAuthManager.isSignedIn(projectId)

    override fun getCoreApp(): FirebaseApp = firebaseAuthManager.getCoreApp()

    override fun getLegacyAppFallback(): FirebaseApp = firebaseAuthManager.getLegacyAppFallback()

    override suspend fun <T : SimRemoteInterface> buildClient(remoteInterface: KClass<T>): SimNetwork.SimApiClient<T> =
        simApiClientFactory.buildClient(remoteInterface)
}
