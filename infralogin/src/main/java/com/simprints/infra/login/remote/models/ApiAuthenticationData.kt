package com.simprints.infra.login.remote.models

import androidx.annotation.Keep
import com.simprints.infra.login.domain.models.AuthenticationData

@Keep
internal data class ApiAuthenticationData(val publicKey: String, val nonce: String) {

    fun toDomain() = AuthenticationData(publicKey, nonce)
}
