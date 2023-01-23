package com.simprints.infra.login.domain.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var integrityToken: String = "",
    var deviceId: String
)
