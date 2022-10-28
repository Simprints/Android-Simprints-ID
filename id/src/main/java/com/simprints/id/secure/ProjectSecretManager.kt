package com.simprints.id.secure

import com.simprints.infra.login.LoginManager
import com.simprints.infra.security.cryptography.ProjectSecretEncrypter
import javax.inject.Inject

class ProjectSecretManager @Inject constructor(private val loginManager: LoginManager) {

    fun encryptAndStoreAndReturnProjectSecret(
        projectSecret: String,
        publicKey: String
    ): String =
        encryptProjectSecret(
            projectSecret,
            publicKey
        ).also { storeEncryptedProjectSecret(it) }


    private fun encryptProjectSecret(
        projectSecret: String,
        publicKey: String
    ): String =
        ProjectSecretEncrypter(publicKey).encrypt(projectSecret)


    private fun storeEncryptedProjectSecret(encryptedProjectSecret: String) {
        loginManager.encryptedProjectSecret = encryptedProjectSecret
    }

}
