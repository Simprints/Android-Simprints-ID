package com.simprints.id.secure

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.exceptions.safe.ProjectCredentialsMissingException
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

/**      CALLER
    ProjectAuthenticator(secureDataManager).authenticateWithExistingCredentials(nonceScope) **OR**
    ProjectAuthenticator(secureDataManager).authenticateWithNewCredentials(nonceScope, projectId, encryptedProjectSecret)
            .subscribe(
                { token -> print("we got it!!! $token") },
                { e -> handleException(e) }
            )
*/
class ProjectAuthenticator(private val secureDataManager: SecureDataManager) {

    private val apiClient = ApiService().api
    private val projectSecretManager = ProjectSecretManager(secureDataManager)

    @Throws(ProjectCredentialsMissingException::class)
    fun authenticateWithExistingCredentials(nonceScope: NonceScope): Single<String> =
        authenticate(nonceScope, secureDataManager.projectId, SingleJust<String>(secureDataManager.encryptedProjectSecret))

    fun authenticateWithNewCredentials(nonceScope: NonceScope, projectId: String, projectSecret: String): Single<String> =
        authenticate(nonceScope, projectId, getEncryptedProjectSecret(projectSecret))

    private fun authenticate(nonceScope: NonceScope, projectId: String, encryptedProjectSecret: Single<String>): Single<String> =
        combineProjectSecretAndGoogleAttestationObservables(nonceScope, projectId, encryptedProjectSecret)
            .makeAuthRequest()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private fun combineProjectSecretAndGoogleAttestationObservables(nonceScope: NonceScope, projectId: String, encryptedProjectSecret: Single<String>): Single<AuthRequest> =
        Single.zip(
            encryptedProjectSecret,
            getGoogleAttestation(nonceScope),
            combineAuthRequestParameters(projectId)
        )

    private fun getEncryptedProjectSecret(projectSecret: String): Single<String> =
        PublicKeyManager(apiClient).requestPublicKey()
            .flatMap { publicKey -> projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(noneScope: NonceScope): Single<JSONObject>? =
        NonceManager(apiClient).requestNonce(noneScope).flatMap { nonce ->
            GoogleManager.requestAttestation(nonce)
        }

    private fun combineAuthRequestParameters(projectId: String): BiFunction<String, JSONObject, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: JSONObject ->
            AuthRequest(encryptedProjectSecret, projectId, attestation)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<String> =
        flatMap { authRequest -> AuthManager.requestAuthToken(authRequest) }
}
