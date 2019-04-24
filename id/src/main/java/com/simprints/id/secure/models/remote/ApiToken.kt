package com.simprints.id.secure.models.remote

import com.simprints.id.secure.models.Token
import io.realm.internal.Keep
import java.io.Serializable

@Keep
data class ApiToken(val legacyToken: String = ""): Serializable

fun ApiToken.toDomainToken(): Token = Token(legacyToken)
