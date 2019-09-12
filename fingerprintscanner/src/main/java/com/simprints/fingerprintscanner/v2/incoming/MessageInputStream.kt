package com.simprints.fingerprintscanner.v2.incoming

import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketRouter

class MessageInputStream(
    private val packetRouter: PacketRouter,
    private val veroResponseAccumulator: VeroResponseAccumulator,
    private val veroEventAccumulator: VeroEventAccumulator,
    private val un20ResponseAccumulator: Un20ResponseAccumulator
) : IncomingConnectable by packetRouter {


}
