package com.simprints.id.secure.models.remote

import com.simprints.id.secure.models.AttestToken
import java.io.Serializable

data class ApiToken(val legacyToken: String = ""): Serializable

fun ApiToken.toDomainToken(): AttestToken = AttestToken(legacyToken)
