package com.simprints.id.exceptions.unsafe


class MismatchedTypeError(message: String = "MismatchedTypeError", cause: Throwable)
    : SimprintsError(message, cause)
