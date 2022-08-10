package com.simprints.infra.login.domain.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var safetyNetAttestationResult: String = "",
    var deviceId: String
)
