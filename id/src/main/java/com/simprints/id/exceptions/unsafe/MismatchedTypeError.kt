package com.simprints.id.exceptions.unsafe


class MismatchedTypeError(message: String = "MismatchedTypeError", cause: Throwable)
    : Error(message, cause)
