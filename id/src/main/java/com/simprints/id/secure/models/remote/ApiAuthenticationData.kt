package com.simprints.id.secure.models.remote

import androidx.annotation.Keep
import com.simprints.id.secure.models.AuthenticationData
import com.simprints.id.secure.models.Nonce
import com.simprints.id.secure.models.PublicKeyString

@Keep
data class ApiAuthenticationData(val nonce: String, val publicKeyString: String)

fun ApiAuthenticationData.toDomainAuthData() = AuthenticationData(Nonce(nonce), PublicKeyString(publicKeyString))

