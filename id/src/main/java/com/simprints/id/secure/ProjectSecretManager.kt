package com.simprints.id.secure

import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.secure.cryptography.ProjectSecretEncrypter
import com.simprints.id.secure.models.PublicKeyString

class ProjectSecretManager(private val loginInfoManager: LoginInfoManager) {

    fun encryptAndStoreAndReturnProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): String =
        encryptProjectSecret(projectSecret, publicKeyString)
            .also { storeEncryptedProjectSecret(it) }

    private fun encryptProjectSecret(projectSecret: String, publicKeyString: PublicKeyString): String =
        ProjectSecretEncrypter(publicKeyString).encrypt(projectSecret)

    private fun storeEncryptedProjectSecret(encryptedProjectSecret: String) {
        loginInfoManager.encryptedProjectSecret = encryptedProjectSecret
    }
}
