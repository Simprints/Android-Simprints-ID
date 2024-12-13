package com.simprints.infra.authlogic.authenticator.remote.models

import androidx.annotation.Keep
import com.simprints.infra.authstore.domain.models.AuthenticationData

@Keep
internal data class ApiAuthenticationData(
    val nonce: String,
) {
    fun toDomain() = AuthenticationData(nonce)
}
