package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SafeException


class AuthRequestInvalidCredentialsException(message: String = "AuthRequestInvalidCredentialsException")
    : SafeException(message)
