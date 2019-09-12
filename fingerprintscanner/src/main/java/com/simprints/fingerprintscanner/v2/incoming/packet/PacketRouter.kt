package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.v2.domain.packet.IncomingChannel
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.IncomingConnectable
import com.simprints.fingerprintscanner.v2.tools.toFlowable
import com.simprints.fingerprintscanner.v2.tools.values
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream

class PacketRouter(private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator) : IncomingConnectable {

    private lateinit var inputStream: InputStream

    private lateinit var incomingPackets: ConnectableFlowable<Packet>
    lateinit var incomingPacketChannels: Map<IncomingChannel, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketChannelsDisposable: Map<IncomingChannel, Disposable>

    override fun connect(inputStream: InputStream) {
        this.inputStream = inputStream
        configureIncomingPacketStream(transformToPacketStream(this.inputStream))
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketChannelsDisposable = incomingPacketChannels.mapValues { it.value.connect() }
    }

    private fun configureIncomingPacketStream(rawPacketStream: Flowable<Packet>) {
        incomingPackets = rawPacketStream.subscribeAndPublish()
        incomingPacketChannels = IncomingChannel::class.values().associateWith {
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

    private fun ConnectableFlowable<Packet>.filterChannel(channel: IncomingChannel) =
        filter { packet -> packet.source == channel.id }

    private fun Flowable<Packet>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()
}
