package com.simprints.fingerprint.scanner.exceptions.unexpected

import com.simprints.fingerprint.exceptions.unexpected.FingerprintUnexpectedException


class NullScannerException(message: String = "NullScannerException") : FingerprintUnexpectedException(message)
