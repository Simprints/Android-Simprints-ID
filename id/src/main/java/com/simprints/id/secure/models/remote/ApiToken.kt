package com.simprints.id.secure.models.remote

import com.simprints.id.secure.models.Token
import java.io.Serializable

data class ApiToken(val legacyToken: String = ""): Serializable

fun ApiToken.toDomainToken(): Token = Token(legacyToken)
