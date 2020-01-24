package com.simprints.fingerprintscanner.v2.ota.stm

import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryAddressCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryDataCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.stream.StmOtaMessageStream
import com.simprints.fingerprintscanner.v2.tools.hexparser.FirmwareByteChunk
import com.simprints.fingerprintscanner.v2.tools.hexparser.IntelHexParser
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StmOtaController(private val intelHexParser: IntelHexParser) {

    private inline fun <reified R : StmOtaResponse> sendStmOtaModeCommandAndReceiveResponse(stmOtaMessageStream: StmOtaMessageStream, command: StmOtaCommand): Single<R> =
        Single.defer {
            stmOtaMessageStream.outgoing.sendMessage(command)
                .andThen(stmOtaMessageStream.incoming.receiveResponse<R>())
        }

    fun program(stmOtaMessageStream: StmOtaMessageStream, firmwareHexFile: String): Observable<Float> =
        single {
            intelHexParser.parse(firmwareHexFile)
        }.map { chunkList ->
            chunkList.mapIndexed { index, chunk ->
                Pair(chunk, (index + 1).toFloat() / chunkList.size.toFloat())
            }
        }.flattenAsObservable {
            it
        }.concatMap { (chunk, progress) ->
            sendStmPacket(stmOtaMessageStream, chunk)
                .andThen(Observable.just(progress))
        }

    private fun sendStmPacket(stmOtaMessageStream: StmOtaMessageStream, firmwareByteChunk: FirmwareByteChunk): Completable =
        sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageStream,
            WriteMemoryStartCommand()
        ).verifyResponseIsAck().andThen(
            sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageStream,
                WriteMemoryAddressCommand(firmwareByteChunk.address)
            )
        ).verifyResponseIsAck().andThen(
            sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageStream,
                WriteMemoryDataCommand(firmwareByteChunk.data)
            )
        ).verifyResponseIsAck()

    private fun Single<out CommandAcknowledgement>.verifyResponseIsAck(): Completable =
        flatMapCompletable {
            completable {
                if (it.kind != CommandAcknowledgement.Kind.ACK) {
                    throw TODO("Received NACK response during STM OTA")
                }
            }
        }
}
