package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream

class PacketRouter(private val routes: List<Route>,
                   private inline val packetRouteDesignator: Packet.() -> Byte,
                   private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator) : IncomingConnectable {

    private lateinit var incomingPackets: ConnectableFlowable<Packet>
    lateinit var incomingPacketRoutes: Map<Route, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketRoutesDisposable: Map<Route, Disposable>

    override fun connect(inputStream: InputStream) {
        configureIncomingPacketStream(transformToPacketStream(inputStream))
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketRoutesDisposable = incomingPacketRoutes.mapValues { it.value.connect() }
    }

    private fun configureIncomingPacketStream(rawPacketStream: Flowable<Packet>) {
        incomingPackets = rawPacketStream.subscribeAndPublish()
        incomingPacketRoutes = routes.associateWith {
            incomingPackets.filterRoute(it).subscribeAndPublish()
        }
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

    private fun Flowable<Packet>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()
}
