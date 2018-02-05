package com.simprints.id.secure

import com.simprints.id.secure.cryptography.AsymmetricEncrypter
import com.simprints.id.secure.models.PublicKeyString
import io.reactivex.Single
import io.reactivex.internal.operators.single.SingleJust

class ProjectSecretManager {
    companion object {

        fun getEncryptedProjectSecret(): Single<String> {
            return SingleJust<String>("") //TODO: Exception if project Secret encrypted is not in shared
        }

        fun encryptAndStoreProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): Single<String> {
            val encryptedProjectSecret = encryptProjectSecret(projectSecret, publicKeyString)
            //storeEncryptedProjectSecret(encryptedProjectSecret)
            return encryptedProjectSecret
        }

        private fun encryptProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): Single<String> =
            SingleJust<String>(AsymmetricEncrypter(publicKeyString).encrypt(projectSecret))

        private fun storeEncryptedProjectSecret(encryptedProjectSecret: Single<String>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
