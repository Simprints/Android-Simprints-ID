package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.common.MessageOutputStream
import io.reactivex.Flowable
import java.io.OutputStream

/**
 * Base class to hold onto pairs of corresponding [MessageInputStream]s and [MessageOutputStream]s
 * for easier sending and receiving.
 *
 * It was found to be better to start observing the response at the same time/slightly before
 * sending the command as sometimes the Vero would respond too quickly.
 */
abstract class MessageChannel<I : MessageInputStream, O : MessageOutputStream<*>>(
    val incoming: I,
    val outgoing: O
) : Connectable {

    override fun connect(flowableInputStream: Flowable<ByteArray>, outputStream: OutputStream) {
        incoming.connect(flowableInputStream)
        outgoing.connect(outputStream)
    }

    override fun disconnect() {
        outgoing.disconnect()
        incoming.disconnect()
    }
}
