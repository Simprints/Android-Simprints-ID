package com.simprints.id.secure

import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.security.cryptography.ProjectSecretEncrypter

class ProjectSecretManager(private val loginInfoManager: LoginInfoManager) {

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
        loginInfoManager.encryptedProjectSecret = encryptedProjectSecret
    }

}
