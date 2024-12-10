package com.simprints.fingerprint.infra.scanner.v2.incoming.main

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.incoming.common.MessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.toMainMessageStream
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprint.infra.scanner.v2.tools.lang.isSubclass
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.filterCast
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

/**
 * Transforms each of the Flowable<Packet> streams exposed by [PacketRouter] into a
 * Flowable<Message> for each [Route].
 *
 * [receiveResponse] can be used to take a message from the [veroResponses] or [un20Responses]
 * streams, while the [veroEvents] stream can be observed directly.
 */
class MainMessageInputStream @Inject constructor(
    private val packetRouter: PacketRouter,
    private val veroResponseAccumulator: VeroResponseAccumulator,
    private val veroEventAccumulator: VeroEventAccumulator,
    private val un20ResponseAccumulator: Un20ResponseAccumulator,
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : MessageInputStream {

    var veroResponses: Flowable<VeroResponse>? = null
    var veroEvents: Flowable<VeroEvent>? = null
    var un20Responses: Flowable<Un20Response>? = null

    private var veroResponsesDisposable: Disposable? = null
    private var veroEventsDisposable: Disposable? = null
    private var un20ResponsesDisposable: Disposable? = null

    override fun connect(flowableInputStream: Flowable<ByteArray>) {
        packetRouter.connect(flowableInputStream)
        with(packetRouter.incomingPacketRoutes) {
            veroResponses =
                getValue(Route.Remote.VeroServer).toMainMessageStream(veroResponseAccumulator)
                    .subscribeOnIoAndPublish(ioDispatcher)
                    .also { veroResponsesDisposable = it.connect() }
            veroEvents = getValue(Route.Remote.VeroEvent).toMainMessageStream(veroEventAccumulator)
                .subscribeOnIoAndPublish(ioDispatcher).also { veroEventsDisposable = it.connect() }
            un20Responses =
                getValue(Route.Remote.Un20Server).toMainMessageStream(un20ResponseAccumulator)
                    .subscribeOnIoAndPublish(ioDispatcher)
                    .also { un20ResponsesDisposable = it.connect() }
        }
    }

    override fun disconnect() {
        veroResponsesDisposable?.dispose()
        veroEventsDisposable?.dispose()
        un20ResponsesDisposable?.dispose()
        packetRouter.disconnect()
    }

    inline fun <reified R : IncomingMainMessage> receiveResponse(): Single<R> =
        Single.defer {
            when {
                isSubclass<R, VeroResponse>() -> veroResponses
                isSubclass<R, VeroEvent>() -> veroEvents
                isSubclass<R, Un20Response>() -> un20Responses
                else -> Flowable.error(IllegalArgumentException("Trying to receive invalid response"))
            }
                ?.filterCast<R>()
                ?.firstOrError()
                ?: Single.error(IllegalStateException("Trying to receive response before connecting stream"))
        }
}
