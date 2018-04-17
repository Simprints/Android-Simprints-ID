package com.simprints.id.exceptions.safe.secure

import com.simprints.id.exceptions.safe.SimprintsException


class AuthRequestInvalidCredentialsException(message: String = "AuthRequestInvalidCredentialsException")
    : SimprintsException(message)
