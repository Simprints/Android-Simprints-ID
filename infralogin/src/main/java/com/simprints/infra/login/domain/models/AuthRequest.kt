package com.simprints.infra.login.domain.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var integrityAPIVerdict: String = "",
    var deviceId: String
)
