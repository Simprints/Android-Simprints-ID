package com.simprints.id.secure.models

data class AuthRequest(
    var encryptedProjectSecret: String = "",
    var projectId: String = "",
    var userId: String = "",
    var attestation: AttestToken = AttestToken(""))
