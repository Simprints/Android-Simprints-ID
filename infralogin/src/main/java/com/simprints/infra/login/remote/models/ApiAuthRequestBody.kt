package com.simprints.infra.login.remote.models

import androidx.annotation.Keep
import com.simprints.infra.login.domain.models.AuthRequest

@Keep
internal data class ApiAuthRequestBody(
    var encryptedProjectSecret: String = "",
    var integrityAPIVerdict: String = "",
    var deviceId: String
) {
    companion object {
        fun fromDomain(authRequest: AuthRequest): ApiAuthRequestBody =
            ApiAuthRequestBody(
                authRequest.encryptedProjectSecret,
                authRequest.integrityAPIVerdict,
                authRequest.deviceId,
            )
    }
}
