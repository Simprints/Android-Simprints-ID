package com.simprints.fingerprintscanner.v2.outgoing.main

import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.outgoing.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable

class MainMessageOutputStream(
    private val mainMessageSerializer: MainMessageSerializer,
    private val outputStreamDispatcher: OutputStreamDispatcher
) : OutgoingConnectable by outputStreamDispatcher {

    fun sendMessage(message: OutgoingMainMessage): Completable =
        single {
            mainMessageSerializer.serialize(message)
        }.flatMapCompletable {
            outputStreamDispatcher.dispatch(it)
        }
}
