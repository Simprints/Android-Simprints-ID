package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.subscribeOnIoAndPublish
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable

/**
 * The PacketRouter takes an InputStream of bytes, converting it into a single stream of
 * Flowable<Packet> containing all incoming packets. It reads the [Route] of each incoming packet,
 * and forwards them to a separate Flowable<Packet> dedicated to that route, which is exposed
 * in the map [incomingPacketRoutes].
 */
class PacketRouter(private val routes: List<Route>,
                   private val packetRouteDesignator: Packet.() -> Byte,
                   private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator) : IncomingConnectable {

    lateinit var incomingPacketRoutes: Map<Route, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketRoutesDisposable: Map<Route, Disposable>

    override fun connect(flowableInputStream: Flowable<ByteArray>) {
        val rawPacketStream = transformToPacketStream(flowableInputStream)
        val incomingPackets = rawPacketStream.subscribeOnIoAndPublish()
        incomingPacketRoutes = routes.associateWith {
            incomingPackets.filterRoute(it).subscribeOnIoAndPublish()
        }
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketRoutesDisposable = incomingPacketRoutes.mapValues { it.value.connect() }
    }

    private fun transformToPacketStream(flowableInputStream: Flowable<ByteArray>): Flowable<Packet> =
        flowableInputStream.toPacketStream(byteArrayToPacketAccumulator)

    override fun disconnect() {
        incomingPacketsDisposable.dispose()
        incomingPacketRoutesDisposable.forEach { it.value.dispose() }
    }

    private fun ConnectableFlowable<Packet>.filterRoute(route: Route) =
        filter { packet -> packet.packetRouteDesignator() == route.id.value }
}
