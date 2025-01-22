package com.simprints.fingerprint.infra.scanner.v2.tools

import com.simprints.fingerprint.infra.scanner.exceptions.safe.ScannerDisconnectedException
import com.simprints.fingerprint.infra.scanner.exceptions.unexpected.UnexpectedScannerException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.exceptions.state.NotConnectedException
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

fun wrapErrorFromScanner(e: Throwable): Throwable = when (e) {
    is CancellationException -> e // Propagate cancellation
    is NotConnectedException,
    is IOException,
    -> { // Disconnected or timed-out communications with Scanner
        Simber.i("IOException in ScannerWrapperV2, transformed to ScannerDisconnectedException", e, tag = FINGER_CAPTURE)
        ScannerDisconnectedException()
    }

    is IllegalStateException, // We're calling scanner methods out of order somehow
    is IllegalArgumentException,
    -> {
        Simber.e("Received unexpected/invalid bytes from the scanner", e, tag = FINGER_CAPTURE)
        UnexpectedScannerException(throwable = e)
    }

    is OtaFailedException -> { // Wrap the OTA failed exception to fingerprint domain exception
        OtaFailedException("Wrapped OTA failed exception from scanner", e)
    }

    else -> { // Propagate error
        e
    }
}

suspend fun <T> runWithErrorWrapping(block: suspend () -> T): T = try {
    block()
} catch (e: Exception) {
    throw wrapErrorFromScanner(e)
}
