package com.simprints.id.secure

import com.simprints.core.login.LoginInfoManager
import com.simprints.id.secure.models.PublicKeyString
import com.simprints.infra.security.cryptography.ProjectSecretEncrypter

class ProjectSecretManager(private val loginInfoManager: LoginInfoManager) {

    fun encryptAndStoreAndReturnProjectSecret(
        projectSecret: String,
        publicKeyString: PublicKeyString
    ): String =
        encryptProjectSecret(projectSecret, publicKeyString)
            .also { storeEncryptedProjectSecret(it) }

    private fun encryptProjectSecret(
        projectSecret: String,
        publicKeyString: PublicKeyString
    ): String =
        ProjectSecretEncrypter(publicKeyString.value).encrypt(projectSecret)

    private fun storeEncryptedProjectSecret(encryptedProjectSecret: String) {
        loginInfoManager.encryptedProjectSecret = encryptedProjectSecret
    }
}
