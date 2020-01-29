package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.subscribeOnIoAndPublish
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import java.io.InputStream

class PacketRouter(private val routes: List<Route>,
                   private inline val packetRouteDesignator: Packet.() -> Byte,
                   private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator) : IncomingConnectable {

    lateinit var incomingPacketRoutes: Map<Route, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketRoutesDisposable: Map<Route, Disposable>

    override fun connect(inputStream: InputStream) {
        val rawPacketStream = transformToPacketStream(inputStream)
        val incomingPackets = rawPacketStream.subscribeOnIoAndPublish()
        incomingPacketRoutes = routes.associateWith {
            incomingPackets.filterRoute(it).subscribeOnIoAndPublish()
        }
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketRoutesDisposable = incomingPacketRoutes.mapValues { it.value.connect() }
    }

    private fun transformToPacketStream(inputStream: InputStream): Flowable<Packet> =
        inputStream
            .toFlowable()
            .toPacketStream(byteArrayToPacketAccumulator)

    override fun disconnect() {
        incomingPacketsDisposable.dispose()
        incomingPacketRoutesDisposable.forEach { it.value.dispose() }
    }

    private fun ConnectableFlowable<Packet>.filterRoute(route: Route) =
        filter { packet -> packet.packetRouteDesignator() == route.id.value }
}
