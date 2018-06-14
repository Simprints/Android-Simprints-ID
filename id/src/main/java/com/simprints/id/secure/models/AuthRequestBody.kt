package com.simprints.id.secure.models

data class AuthRequestBody(var encryptedProjectSecret: String = "",
                           var safetyNetAttestationResult: String = "")
