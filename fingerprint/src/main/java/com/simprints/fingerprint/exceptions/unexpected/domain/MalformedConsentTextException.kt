package com.simprints.fingerprint.exceptions.unexpected.domain

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException


class MalformedConsentTextException(message: String = "MalformedConsentTextException", cause: Throwable) : FingerprintUnexpectedException(message, cause)
