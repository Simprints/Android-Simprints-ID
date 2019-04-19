package com.simprints.id.secure.models

import androidx.annotation.Keep

@Keep
data class AuthRequest(
    var projectId: String = "",
    var userId: String = "",
    var authRequestBody: AuthRequestBody)
