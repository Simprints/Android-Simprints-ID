package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.incoming.IncomingConnectable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The PacketRouter takes an InputStream of bytes, converting it into a single stream of
 * Flow <Packet> containing all incoming packets. It reads the [Route] of each incoming packet,
 * and forwards them to a separate Flow <Packet> dedicated to that route, which is exposed
 * in the map [PacketRouter.incomingPacketRoutes].
 */

class PacketRouter @Inject constructor(
    routes: List<Route>,
    private val packetRouteDesignator: Packet.() -> Byte,
    private val byteArrayToPacketAccumulator: ByteArrayToPacketAccumulator,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : IncomingConnectable {
    private val routeIdMap = routes.associateBy { it.id.value }
    private val internalPacketRoutes = routes.associateWith { MutableSharedFlow<Packet>() }
    val incomingPacketRoutes: Map<Route, Flow<Packet>> = internalPacketRoutes.mapValues { it.value.asSharedFlow() }

    var packetProcessingJob: Job? = null
        private set // Expose for testing but restrict external modification

    override fun connect(inputStream: Flow<ByteArray>) {
        packetProcessingJob?.cancel()
        packetProcessingJob = CoroutineScope(dispatcher).launch {
            inputStream
                .toPacketStream(byteArrayToPacketAccumulator)
                .collect { packet ->
                    routeIdMap[packet.packetRouteDesignator()]?.let { internalPacketRoutes[it] }?.emit(packet)
                }
        }
    }

    override fun disconnect() {
        packetProcessingJob?.cancel()
        internalPacketRoutes.values.forEach { it.resetReplayCache() }
    }
}
