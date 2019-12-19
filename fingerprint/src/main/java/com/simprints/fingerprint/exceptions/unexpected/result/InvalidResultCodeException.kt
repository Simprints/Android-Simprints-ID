package com.simprints.fingerprint.exceptions.unexpected.result

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException

class InvalidResultCodeException(message: String = "InvalidResultCodeException") : FingerprintUnexpectedException(message) {

    companion object {
        fun forResultCode(code: Int) = InvalidResultCodeException("Invalid task result code: $code")
    }
}
