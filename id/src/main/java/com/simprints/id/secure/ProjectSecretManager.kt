package com.simprints.id.secure

import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import com.simprints.id.secure.models.PublicKeyString
import io.reactivex.Single
import io.reactivex.internal.operators.single.SingleJust

class ProjectSecretManager(private val secureDataManager: SecureDataManager) {

    fun encryptAndStoreAndReturnProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): Single<String> {
        val encryptedProjectSecret = encryptProjectSecret(projectSecret, publicKeyString)
        storeEncryptedProjectSecret(encryptedProjectSecret)
        return encryptedProjectSecret
    }

    private fun encryptProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): Single<String> =
        SingleJust<String>(AsymmetricEncrypter(publicKeyString).encrypt(projectSecret))

    private fun storeEncryptedProjectSecret(encryptedProjectSecret: Single<String>) {
        secureDataManager.encryptedProjectSecret = encryptedProjectSecret.blockingGet()
    }
}
