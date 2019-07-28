package com.simprints.fingerprint.exceptions.unexpected.scanner

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException


class NullScannerException(message: String = "NullScannerException") : FingerprintUnexpectedException(message)
