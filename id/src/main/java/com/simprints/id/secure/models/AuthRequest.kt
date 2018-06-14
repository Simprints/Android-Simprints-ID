package com.simprints.id.secure.models

data class AuthRequest(
    var projectId: String = "",
    var userId: String = "",
    var authRequestBody: AuthRequestBody)
