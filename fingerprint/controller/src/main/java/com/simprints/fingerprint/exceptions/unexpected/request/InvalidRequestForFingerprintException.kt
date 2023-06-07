package com.simprints.fingerprint.exceptions.unexpected.request

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class InvalidRequestForFingerprintException(message: String = "InvalidRequestForFingerprintException") : FingerprintUnexpectedException(message)
