package com.simprints.id.secure

import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.models.AttestToken
import com.simprints.id.secure.models.AuthRequest
import com.simprints.id.secure.models.NonceScope
import com.simprints.id.secure.models.Token
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers

/**      CALLER
ProjectAuthenticator(secureDataManager).authenticateWithExistingCredentials(nonceScope) **OR**
ProjectAuthenticator(secureDataManager).authenticateWithNewCredentials(nonceScope, projectId, encryptedProjectSecret)
.subscribe(
{ token -> print("we got it!!! $token") },
{ e -> handleException(e) }
)
 */
class ProjectAuthenticator(private val secureDataManager: SecureDataManager,
                           private val dataManager: DbManager,
                           apiClient: ApiServiceInterface = ApiService().api) {

    private val projectSecretManager = ProjectSecretManager(secureDataManager)
    private val publicKeyManager = PublicKeyManager(apiClient)
    private val nonceManager = NonceManager(apiClient)
    private val authManager = AuthManager(apiClient)
    var attestationManager = AttestationManager()

    fun authenticateWithNewCredentials(safetyNetClient: SafetyNetClient, nonceScope: NonceScope, projectSecret: String): Single<Token> =
        authenticate(safetyNetClient, nonceScope, getEncryptedProjectSecret(projectSecret))

    private fun authenticate(safetyNetClient: SafetyNetClient, nonceScope: NonceScope, encryptedProjectSecret: Single<String>): Single<Token> =
        combineProjectSecretAndGoogleAttestationObservables(safetyNetClient, nonceScope, encryptedProjectSecret)
            .makeAuthRequest()
            .initFirebase(nonceScope.projectId)
            .observeOn(AndroidSchedulers.mainThread())

    private fun combineProjectSecretAndGoogleAttestationObservables(safetyNetClient: SafetyNetClient, nonceScope: NonceScope, encryptedProjectSecret: Single<String>): Single<AuthRequest> =
        Single.zip(
            encryptedProjectSecret.subscribeOn(Schedulers.io()),
            getGoogleAttestation(safetyNetClient, nonceScope).subscribeOn(Schedulers.io()),
            combineAuthRequestParameters(nonceScope.projectId, nonceScope.userId)
        )

    private fun getEncryptedProjectSecret(projectSecret: String): Single<String> =
        publicKeyManager.requestPublicKey()
            .flatMap { publicKey -> projectSecretManager.encryptAndStoreAndReturnProjectSecret(projectSecret, publicKey) }

    private fun getGoogleAttestation(safetyNetClient: SafetyNetClient, noneScope: NonceScope): Single<AttestToken> =
        nonceManager.requestNonce(noneScope).flatMap { nonce ->
            attestationManager
                .requestAttestation(safetyNetClient, nonce)
                .subscribeOn(Schedulers.io())
        }

    private fun combineAuthRequestParameters(projectId: String, userId: String): BiFunction<String, AttestToken, AuthRequest> =
        BiFunction { encryptedProjectSecret: String, attestation: AttestToken ->
            AuthRequest(encryptedProjectSecret, projectId, userId, attestation)
        }

    private fun Single<out Token>.initFirebase(projectId: String): Single<Token> =
        flatMap { token ->
            if (!dataManager.isDbInitialised(projectId)) {
                dataManager.initialiseDb(projectId)
            }
            dataManager.signIn(projectId, token)
            SingleJust(token)
        }

    private fun Single<out AuthRequest>.makeAuthRequest(): Single<Token> =
        flatMap { authRequest ->
            authManager
            .requestAuthToken(authRequest)
            .subscribeOn(Schedulers.io())
        }
}
