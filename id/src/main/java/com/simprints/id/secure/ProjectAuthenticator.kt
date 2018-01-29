package com.simprints.id.secure


class ProjectAuthenticator {

    fun authenticate(nonceBundle: NonceBundle, projectId: String, encryptedProjectSecret: String): ProjectAuthenticationResult {
        val nonce = getNonce(nonceBundle)
        val token = getAttestationToken(nonce)
        return runValidation(nonce, token, projectId, encryptedProjectSecret)
    }

    private fun getNonce(nonceBundle: NonceBundle): String {
        TODO("not implemented")
    }

    private fun getAttestationToken(nonce: String): AttestToken {
        TODO("not implemented")
    }

    private fun runValidation(nonce: String,
                              token: AttestToken,
                              projectId: String,
                              encryptedProjectSecret: String)
        : ProjectAuthenticationResult {
        TODO("not implemented")
    }
}
