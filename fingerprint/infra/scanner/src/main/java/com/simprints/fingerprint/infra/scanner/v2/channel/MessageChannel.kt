package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.OutputStream

/**
 * Represents a message channel with incoming and outgoing streams,
 * providing thread-safe and coroutine-friendly operations.
 *
 * @param I The type of the input stream, must extend [MessageInputStream].
 * @param O The type of the output stream, must extend [MessageOutputStream].
 * @property incoming The incoming stream for reading data.
 * @property outgoing The outgoing stream for writing data.
 * @property readWriteLock A mutex for synchronizing read/write access.
 * @property responseErrorHandler Handles errors during execution.
 * @property dispatcher The coroutine dispatcher for executing tasks.
 */
abstract class MessageChannel<I : MessageInputStream, O : MessageOutputStream<*>>(
    val incoming: I,
    val outgoing: O,
    val dispatcher: CoroutineDispatcher,
    val readWriteLock: Mutex = Mutex(),
    val responseErrorHandler: ResponseErrorHandler = ResponseErrorHandler(
        ResponseErrorHandlingStrategy.DEFAULT,
    ),
) : Connectable {
    override fun connect(
        flowableInputStream: Flowable<ByteArray>,
        outputStream: OutputStream,
    ) {
        incoming.connect(flowableInputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }

    suspend inline fun <reified R> runLockedTask(crossinline block: suspend () -> R): R =
        withContext(dispatcher) { readWriteLock.withLock { responseErrorHandler.handle(block) } }
}
