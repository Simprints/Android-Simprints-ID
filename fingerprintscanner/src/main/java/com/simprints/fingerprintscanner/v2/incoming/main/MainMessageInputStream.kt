package com.simprints.fingerprintscanner.v2.incoming.main

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.toMainMessageStream
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.tools.lang.isSubclass
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.io.InputStream

class MainMessageInputStream(
    private val packetRouter: PacketRouter,
    private val veroResponseAccumulator: VeroResponseAccumulator,
    private val veroEventAccumulator: VeroEventAccumulator,
    private val un20ResponseAccumulator: Un20ResponseAccumulator
) : MessageInputStream {

    var veroResponses: Flowable<VeroResponse>? = null
    var veroEvents: Flowable<VeroEvent>? = null
    var un20Responses: Flowable<Un20Response>? = null

    private var veroResponsesDisposable: Disposable? = null
    private var veroEventsDisposable: Disposable? = null
    private var un20ResponsesDisposable: Disposable? = null

    override fun connect(inputStream: InputStream) {
        packetRouter.connect(inputStream)
        with(packetRouter.incomingPacketRoutes) {
            veroResponses = getValue(Route.Remote.VeroServer).toMainMessageStream(veroResponseAccumulator)
                .subscribeOnIoAndPublish().also { veroResponsesDisposable = it.connect() }
            veroEvents = getValue(Route.Remote.VeroEvent).toMainMessageStream(veroEventAccumulator)
                .subscribeOnIoAndPublish().also { veroEventsDisposable = it.connect() }
            un20Responses = getValue(Route.Remote.Un20Server).toMainMessageStream(un20ResponseAccumulator)
                .subscribeOnIoAndPublish().also { un20ResponsesDisposable = it.connect() }
        }
    }

    override fun disconnect() {
        veroResponsesDisposable?.dispose()
        veroEventsDisposable?.dispose()
        un20ResponsesDisposable?.dispose()
        packetRouter.disconnect()
    }

    inline fun <reified R : IncomingMainMessage> receiveResponse(crossinline withPredicate: (R) -> Boolean = { true }): Single<R> =
        Single.defer {
            when {
                isSubclass<R, VeroResponse>() -> veroResponses
                isSubclass<R, VeroEvent>() -> veroEvents
                isSubclass<R, Un20Response>() -> un20Responses
                else -> Flowable.error(IllegalArgumentException("Trying to receive invalid response"))
            }
                ?.filterCast(withPredicate)
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
