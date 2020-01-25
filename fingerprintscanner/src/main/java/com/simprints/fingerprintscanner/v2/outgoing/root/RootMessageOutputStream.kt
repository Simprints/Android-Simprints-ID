package com.simprints.fingerprintscanner.v2.outgoing.root

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.outgoing.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable

class RootMessageOutputStream(
    private val rootMessageSerializer: RootMessageSerializer,
    private val outputStreamDispatcher: OutputStreamDispatcher
) : OutgoingConnectable by outputStreamDispatcher {

    fun sendMessage(message: RootCommand): Completable =
        single {
            rootMessageSerializer.serialize(message)
        }.flatMapCompletable {
            outputStreamDispatcher.dispatch(it)
        }
}
