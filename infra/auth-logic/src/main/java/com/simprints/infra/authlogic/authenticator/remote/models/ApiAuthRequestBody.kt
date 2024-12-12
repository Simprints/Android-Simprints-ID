package com.simprints.infra.authlogic.authenticator.remote.models

import androidx.annotation.Keep
import com.simprints.infra.authstore.domain.models.AuthRequest

@Keep
internal data class ApiAuthRequestBody(
    var projectSecret: String = "",
    var integrityToken: String = "",
) {
    companion object {
        fun fromDomain(authRequest: AuthRequest): ApiAuthRequestBody = ApiAuthRequestBody(
            authRequest.projectSecret,
            authRequest.integrityToken,
        )
    }
}
