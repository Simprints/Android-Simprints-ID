package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream

class PacketRouter(private val channels: List<Channel>,
                   private inline val packetChannelDesignator: Packet.() -> Byte,
                   private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator) : IncomingConnectable {

    private lateinit var inputStream: InputStream

    private lateinit var incomingPackets: ConnectableFlowable<Packet>
    lateinit var incomingPacketChannels: Map<Channel, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketChannelsDisposable: Map<Channel, Disposable>

    override fun connect(inputStream: InputStream) {
        this.inputStream = inputStream
        configureIncomingPacketStream(transformToPacketStream(this.inputStream))
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketChannelsDisposable = incomingPacketChannels.mapValues { it.value.connect() }
    }

    private fun configureIncomingPacketStream(rawPacketStream: Flowable<Packet>) {
        incomingPackets = rawPacketStream.subscribeAndPublish()
        incomingPacketChannels = channels.associateWith {
            incomingPackets.filterChannel(it).subscribeAndPublish()
        }
    }

    private fun transformToPacketStream(inputStream: InputStream): Flowable<Packet> =
        inputStream
            .toFlowable()
            .toPacketStream(byteArrayToPacketAccumulator)

    override fun disconnect() {
        incomingPacketsDisposable.dispose()
        incomingPacketChannelsDisposable.forEach { it.value.dispose() }
    }

    private fun ConnectableFlowable<Packet>.filterChannel(channel: Channel) =
        filter { packet -> packet.packetChannelDesignator() == channel.id.value }

    private fun Flowable<Packet>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()
}
