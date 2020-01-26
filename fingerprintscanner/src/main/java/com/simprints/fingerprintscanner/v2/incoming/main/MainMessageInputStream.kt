package com.simprints.fingerprintscanner.v2.incoming.main

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.toMainMessageStream
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.tools.lang.isSubclass
import com.simprints.fingerprintscanner.v2.tools.reactive.filterCast
import io.reactivex.Flowable
import io.reactivex.Single
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

    override fun connect(inputStream: InputStream) {
        packetRouter.connect(inputStream)
        veroResponses = packetRouter.incomingPacketChannels.getValue(Channel.Remote.VeroServer).toMainMessageStream(veroResponseAccumulator)
        veroEvents = packetRouter.incomingPacketChannels.getValue(Channel.Remote.VeroEvent).toMainMessageStream(veroEventAccumulator)
        un20Responses = packetRouter.incomingPacketChannels.getValue(Channel.Remote.Un20Server).toMainMessageStream(un20ResponseAccumulator)
    }

    override fun disconnect() {
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
