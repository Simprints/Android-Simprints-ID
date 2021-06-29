package com.simprints.id.exceptions.safe.secure

import com.simprints.core.exceptions.SafeException


class AuthRequestInvalidCredentialsException(message: String = "AuthRequestInvalidCredentialsException")
    : SafeException(message)
