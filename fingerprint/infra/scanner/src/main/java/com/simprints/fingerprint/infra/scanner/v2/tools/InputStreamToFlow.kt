package com.simprints.fingerprint.infra.scanner.v2.tools

import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.io.InputStream

fun InputStream.asFlow(
    dispatcher: CoroutineDispatcher,
    bufferSize: Int = 1024, //  1 KB is the maximum buffer size supported by the bluetooth stack
): Flow<ByteArray> = flow {
    val buffer = ByteArray(bufferSize)
    try {
        while (true) {
            val count = read(buffer)
            when {
                count == -1 -> break // End of stream
                count < bufferSize -> emit(buffer.copyOf(count)) // Emit only the valid portion
                else -> emit(buffer) // Emit the full buffer
            }
        }
    } catch (e: IOException) {
        // IOExceptions should be ignored because they are thrown when disconnecting the scanner
        // and closing the input stream at the end of the fingerprint collection process
        Simber.i("Scanner disconnected", e, tag = FINGER_CAPTURE)
    }
}.flowOn(dispatcher)
