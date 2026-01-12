package com.simprints.infra.authlogic.authenticator.remote.models

import androidx.annotation.Keep
import com.simprints.infra.authstore.domain.models.AuthenticationData
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiAuthenticationData(
    val nonce: String,
) {
    fun toDomain() = AuthenticationData(nonce)
}
