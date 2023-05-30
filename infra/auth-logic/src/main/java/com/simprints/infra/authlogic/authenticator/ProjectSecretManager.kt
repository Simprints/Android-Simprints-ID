package com.simprints.infra.authlogic.authenticator

import com.simprints.infra.security.cryptography.ProjectSecretEncrypter
import javax.inject.Inject

internal class ProjectSecretManager @Inject constructor() {

    fun encryptProjectSecret(
        projectSecret: String,
        publicKey: String
    ): String = ProjectSecretEncrypter(publicKey).encrypt(projectSecret)

}
