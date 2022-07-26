package com.simprints.infra.login.remote.models

import androidx.annotation.Keep
import com.simprints.infra.login.domain.models.AuthenticationData

@Keep
data class ApiAuthenticationData(val nonce: String, val publicKey: String) {

    fun toDomain() = AuthenticationData(nonce, publicKey)
}

