package com.simprints.id.exceptions.unexpected


class MismatchedTypeError(message: String = "MismatchedTypeError", cause: Throwable)
    : UnexpectedException(message, cause)
