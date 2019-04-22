package com.simprints.id.secure.models

import androidx.annotation.Keep

@Keep
data class AuthRequestBody(var encryptedProjectSecret: String = "",
                           var safetyNetAttestationResult: String = "")
