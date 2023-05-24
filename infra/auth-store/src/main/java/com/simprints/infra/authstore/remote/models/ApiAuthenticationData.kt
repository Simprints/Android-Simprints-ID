package com.simprints.infra.authstore.remote.models

import androidx.annotation.Keep
import com.simprints.infra.authstore.domain.models.AuthenticationData

@Keep
internal data class ApiAuthenticationData(val publicKey: String, val nonce: String) {

    fun toDomain() = AuthenticationData(publicKey, nonce)
}
