package com.simprints.fingerprint.infra.scanner.v2.incoming.main

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Transforms each of the Flow <Packet> streams exposed by [PacketRouter] into a
 * Flow <Message> for each [Route].
 *
 * [receiveResponse] can be used to take a message from the [veroResponses] or [un20Responses]
 * streams, while the [veroEvents] stream can be observed directly.
 */
class MainMessageInputStream @Inject constructor(
    private val packetRouter: PacketRouter,
    private val veroResponseAccumulator: VeroResponseAccumulator,
    private val veroEventAccumulator: VeroEventAccumulator,
    private val un20ResponseAccumulator: Un20ResponseAccumulator,
) : MessageInputStream {
    lateinit var veroResponses: Flow<VeroResponse>
    lateinit var veroEvents: Flow<VeroEvent>
    lateinit var un20Responses: Flow<Un20Response>

    override fun connect(inputStreamFlow: Flow<ByteArray>) {
        packetRouter.connect(inputStreamFlow)
        with(packetRouter.incomingPacketRoutes) {
            veroResponses = getValue(Route.Remote.VeroServer).toMainMessageStream(veroResponseAccumulator)
            veroEvents = getValue(Route.Remote.VeroEvent).toMainMessageStream(veroEventAccumulator)
            un20Responses = getValue(Route.Remote.Un20Server).toMainMessageStream(un20ResponseAccumulator)
        }
    }

    // only disconnecting the packet router as the other three streams are derived from it as shared flows and will be disconnected automatically
    override fun disconnect() = packetRouter.disconnect()

    @ExcludedFromGeneratedTestCoverageReports(
        "This function is already tested in MainMessageInputStreamTest, but it does not appear in the test coverage report because it is an inline function.",
    )
    suspend inline fun <reified R : IncomingMainMessage> receiveResponse(): R = when {
        isSubclass<R, VeroResponse>() -> veroResponses.filterIsInstance<R>().first()
        isSubclass<R, VeroEvent>() -> veroEvents.filterIsInstance<R>().first()
        isSubclass<R, Un20Response>() -> un20Responses.filterIsInstance<R>().first()
        else -> throw IllegalArgumentException("Trying to receive invalid response")
    }
}
