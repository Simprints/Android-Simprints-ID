package com.simprints.fingerprint.infra.scanner.v2.channel

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.rx2.await

class MainMessageChannel(
    incoming: MainMessageInputStream, outgoing: MainMessageOutputStream
) : MessageChannel<MainMessageInputStream, MainMessageOutputStream>(incoming, outgoing) {

    // Mutex to ensure that only one thread can send and receive messages at a time
    val mutex = Mutex()
    /**
     * Sends a command and waits for a response using a mutex to ensure thread safety.
     * The operation is run on the IO dispatcher and uses `doSimultaneously` to send and receive messages concurrently.
     *
     * @param command The outgoing command message to be sent.
     * @return A Single that emits the response of type R.
     */
    inline fun <reified R : IncomingMainMessage> sendMainModeCommandAndReceiveResponse(
        command: OutgoingMainMessage
    ): Single<R> = rxSingle(Dispatchers.IO) {
        mutex.withLock {
            // Send the command and wait for the response simultaneously
            outgoing.sendMessage(command).doSimultaneously(incoming.receiveResponse<R>()).await()
        }
    }
}
