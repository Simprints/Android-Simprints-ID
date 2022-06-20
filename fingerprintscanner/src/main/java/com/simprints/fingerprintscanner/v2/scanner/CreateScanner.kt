package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.root.RootResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.root.RootResponseParser
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaResponseParser
import com.simprints.fingerprintscanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageSerializer
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.fingerprintscanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.un20.Un20OtaController
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Calculator

/**
 * Helper function to build a new [Scanner] instance with manual dependency injection
 */
fun Scanner.Companion.create(): Scanner {
    val mainMessageChannel = MainMessageChannel(
        MainMessageInputStream(
            PacketRouter(
                listOf(Route.Remote.VeroServer, Route.Remote.VeroEvent, Route.Remote.Un20Server),
                { source },
                ByteArrayToPacketAccumulator(PacketParser())
            ),
            VeroResponseAccumulator(VeroResponseParser()),
            VeroEventAccumulator(VeroEventParser()),
            Un20ResponseAccumulator(Un20ResponseParser())
        ),
        MainMessageOutputStream(
            MainMessageSerializer(),
            OutputStreamDispatcher()
        )
    )

    val rootMessageChannel = RootMessageChannel(
        RootMessageInputStream(
            RootResponseAccumulator(RootResponseParser())
        ),
        RootMessageOutputStream(
            RootMessageSerializer(),
            OutputStreamDispatcher()
        )
    )

    val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)
    val scannerInfoReaderHelper = ScannerInfoReaderHelper(
        mainMessageChannel,
        rootMessageChannel,
        responseErrorHandler
    )

    return Scanner(
        mainMessageChannel,
        rootMessageChannel,
        scannerInfoReaderHelper,
        CypressOtaMessageChannel(
            CypressOtaMessageInputStream(
                CypressOtaResponseParser()
            ),
            CypressOtaMessageOutputStream(
                CypressOtaMessageSerializer(),
                OutputStreamDispatcher()
            )
        ),
        StmOtaMessageChannel(
            StmOtaMessageInputStream(
                StmOtaResponseParser()
            ),
            StmOtaMessageOutputStream(
                StmOtaMessageSerializer(),
                OutputStreamDispatcher()
            )
        ),
        CypressOtaController(Crc32Calculator()),
        StmOtaController(),
        Un20OtaController(Crc32Calculator()),
        responseErrorHandler
    )

}
