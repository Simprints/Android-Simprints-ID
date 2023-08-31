package com.simprints.fingerprint.infra.scanner.v2.tools

import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.infra.logging.Simber
import java.io.IOException

fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
    is NotConnectedException,
    is IOException -> { // Disconnected or timed-out communications with Scanner
        Simber.d(
            e,
            "IOException in ScannerWrapperV2, transformed to ScannerDisconnectedException"
        )
        ScannerDisconnectedException()
    }

    is IllegalStateException, // We're calling scanner methods out of order somehow
    is IllegalArgumentException -> { // We've received unexpected/invalid bytes from the scanner
        Simber.e(e)
        UnexpectedScannerException(e)
    }

    is OtaFailedException -> { // Wrap the OTA failed exception to fingerprint domain exception
        OtaFailedException("Wrapped OTA failed exception from scanner", e)
    }

    else -> { // Propagate error
        e
    }
}
