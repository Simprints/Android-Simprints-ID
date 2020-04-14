package com.simprints.id.exceptions.unexpected

import com.simprints.id.exceptions.UnexpectedException


class MismatchedTypeException(message: String = "MismatchedTypeException", cause: Throwable)
    : UnexpectedException(message, cause)
