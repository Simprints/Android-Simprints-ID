package com.simprints.infra.login

import com.google.firebase.FirebaseApp
import com.simprints.infra.login.db.FirebaseAuthManager
import com.simprints.infra.login.domain.IntegrityTokenRequester
import com.simprints.infra.login.domain.LoginInfoStore
import com.simprints.infra.login.domain.models.AuthRequest
import com.simprints.infra.login.domain.models.AuthenticationData
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.network.SimRemoteInterface
import javax.inject.Inject
import kotlin.reflect.KClass


internal class LoginManagerImpl @Inject constructor(
    private val authenticationRemoteDataSource: AuthenticationRemoteDataSource,
    private val integrityTokenRequester: IntegrityTokenRequester,
    private val loginInfoStore: LoginInfoStore,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val simApiClientFactory: SimApiClientFactory,
) : LoginManager {

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

    override suspend fun requestIntegrityToken(nonce: String): String =
        integrityTokenRequester.getToken(nonce)

    override suspend fun requestAuthenticationData(
        projectId: String,
        userId: String,
        deviceId: String
    ): AuthenticationData =
        authenticationRemoteDataSource.requestAuthenticationData(projectId, userId, deviceId)

    override suspend fun requestAuthToken(
        projectId: String,
        userId: String,
        credentials: AuthRequest
    ): Token = authenticationRemoteDataSource.requestAuthToken(projectId, userId, credentials)

    override fun isProjectIdSignedIn(possibleProjectId: String): Boolean =
        loginInfoStore.isProjectIdSignedIn(possibleProjectId)

    override fun cleanCredentials() {
        loginInfoStore.cleanCredentials()
    }

    override fun storeCredentials(projectId: String, userId: String) {
        loginInfoStore.storeCredentials(projectId, userId)
    }

    override suspend fun signIn(token: Token) {
        firebaseAuthManager.signIn(token)
    }

    override fun signOut() {
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
