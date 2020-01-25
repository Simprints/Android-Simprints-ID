package com.simprints.fingerprintscanner.v2.outgoing.stmota

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.outgoing.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.OutgoingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable

class StmOtaMessageOutputStream(
    private val stmOtaMessageSerializer: StmOtaMessageSerializer,
    private val outputStreamDispatcher: OutputStreamDispatcher
) : OutgoingConnectable by outputStreamDispatcher {

    fun sendMessage(message: StmOtaCommand): Completable =
        single {
            stmOtaMessageSerializer.serialize(message)
        }.flatMapCompletable {
            outputStreamDispatcher.dispatch(it)
        }
}
