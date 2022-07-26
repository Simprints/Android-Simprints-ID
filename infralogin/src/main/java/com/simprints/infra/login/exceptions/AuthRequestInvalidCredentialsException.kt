package com.simprints.infra.login.exceptions

class AuthRequestInvalidCredentialsException(message: String = "AuthRequestInvalidCredentialsException") :
    RuntimeException(message)
