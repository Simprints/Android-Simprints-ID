package com.simprints.fingerprintscanner.v2.outgoing.common

import com.simprints.fingerprintscanner.v2.domain.Message
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.outgoing.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable

abstract class MessageOutputStream<in T : Message>(
    private val messageSerializer: MessageSerializer<T>,
    private val outputStreamDispatcher: OutputStreamDispatcher
) : OutgoingConnectable by outputStreamDispatcher {

    fun sendMessage(message: T): Completable =
        single {
            messageSerializer.serialize(message)
        }.flatMapCompletable {
            outputStreamDispatcher.dispatch(it)
        }
}
