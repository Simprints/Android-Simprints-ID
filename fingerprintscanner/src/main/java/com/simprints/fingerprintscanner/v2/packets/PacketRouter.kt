package com.simprints.fingerprintscanner.v2.packets

import com.simprints.fingerprintscanner.v2.tools.values
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.flowables.ConnectableFlowable
import io.reactivex.schedulers.Schedulers

class PacketRouter {

    private lateinit var incomingPackets: ConnectableFlowable<Packet>
    lateinit var incomingPacketChannels: Map<IncomingChannel, ConnectableFlowable<Packet>>

    private lateinit var incomingPacketsDisposable: Disposable
    private lateinit var incomingPacketChannelsDisposable: Map<IncomingChannel, Disposable>

    fun configureIncomingPacketStream(rawPacketStream: Flowable<Packet>) {
        incomingPackets = rawPacketStream.subscribeAndPublish()
        incomingPacketChannels = IncomingChannel::class.values().associateWith {
            incomingPackets.filterChannel(it).subscribeAndPublish()
        }
    }

    fun connect() {
        incomingPacketsDisposable = incomingPackets.connect()
        incomingPacketChannelsDisposable = incomingPacketChannels.mapValues { it.value.connect() }
    }

    fun disconnect() {
        incomingPacketsDisposable.dispose()
        incomingPacketChannelsDisposable.forEach { it.value.dispose() }
    }

    private fun ConnectableFlowable<Packet>.filterChannel(channel: IncomingChannel) =
        filter { packet -> packet.source == channel.id }

    private fun Flowable<Packet>.subscribeAndPublish() =
        this.subscribeOn(Schedulers.io()).publish()
}
