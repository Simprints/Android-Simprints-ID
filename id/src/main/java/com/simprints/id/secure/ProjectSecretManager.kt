package com.simprints.id.secure

import com.simprints.infra.security.cryptography.ProjectSecretEncrypter
import javax.inject.Inject

class ProjectSecretManager @Inject constructor() {

    fun encryptAndStoreAndReturnProjectSecret(
        projectSecret: String,
        publicKey: String
    ): String = encryptProjectSecret(projectSecret, publicKey)

    private fun encryptProjectSecret(
        projectSecret: String,
        publicKey: String
    ): String =
        ProjectSecretEncrypter(publicKey).encrypt(projectSecret)

}
