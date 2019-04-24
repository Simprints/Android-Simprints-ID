package com.simprints.id.secure.models

import androidx.annotation.Keep

@Keep
data class ApiAuthenticationData(val nonce: String, val publicKeyString: String)
