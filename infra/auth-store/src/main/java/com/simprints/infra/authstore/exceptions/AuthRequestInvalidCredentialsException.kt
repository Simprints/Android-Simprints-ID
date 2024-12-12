package com.simprints.infra.authstore.exceptions

class AuthRequestInvalidCredentialsException(
    message: String = "AuthRequestInvalidCredentialsException",
) : RuntimeException(message)
