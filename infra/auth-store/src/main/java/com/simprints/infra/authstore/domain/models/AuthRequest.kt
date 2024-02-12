package com.simprints.infra.authstore.domain.models

data class AuthRequest(
    var projectSecret: String = "",
    var integrityToken: String = "",
)
