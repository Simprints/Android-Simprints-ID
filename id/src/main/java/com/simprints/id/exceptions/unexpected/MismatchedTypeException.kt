package com.simprints.id.exceptions.unexpected

import com.simprints.core.exceptions.UnexpectedException


class MismatchedTypeException(message: String = "MismatchedTypeException", cause: Throwable)
    : UnexpectedException(message, cause)
