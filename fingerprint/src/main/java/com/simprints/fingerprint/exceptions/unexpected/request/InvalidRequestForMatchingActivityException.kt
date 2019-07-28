package com.simprints.fingerprint.exceptions.unexpected.request

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class InvalidRequestForMatchingActivityException(message: String = "InvalidRequestForMatchingActivityException") : FingerprintUnexpectedException(message)
