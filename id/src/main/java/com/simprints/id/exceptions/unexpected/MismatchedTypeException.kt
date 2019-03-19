package com.simprints.id.exceptions.unexpected


class MismatchedTypeException(message: String = "MismatchedTypeException", cause: Throwable)
    : UnexpectedException(message, cause)
