package com.simprints.infra.login.domain.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var integrityAPIToken: String = "",
    var deviceId: String
)
