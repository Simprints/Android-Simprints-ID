package com.simprints.infra.authstore.domain.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var integrityToken: String = "",
    var deviceId: String
)
