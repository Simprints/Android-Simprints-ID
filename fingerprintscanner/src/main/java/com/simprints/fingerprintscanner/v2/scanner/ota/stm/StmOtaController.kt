package com.simprints.fingerprintscanner.v2.scanner.ota.stm

import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.InitBootloaderCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryAddressCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryDataCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.exceptions.ota.InvalidFirmwareException
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import com.simprints.fingerprintscanner.v2.tools.hexparser.FirmwareByteChunk
import com.simprints.fingerprintscanner.v2.tools.hexparser.IntelHexParser
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StmOtaController(private val intelHexParser: IntelHexParser) {

    private inline fun <reified R : StmOtaResponse> sendStmOtaModeCommandAndReceiveResponse(
        stmOtaMessageChannel: StmOtaMessageChannel,
        errorHandler: ResponseErrorHandler,
        command: StmOtaCommand
    ): Single<R> =
        stmOtaMessageChannel.sendStmOtaModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(errorHandler)

    /**
     * @throws InvalidFirmwareException if firmware file could not be successfully parsed
     * @throws OtaFailedException if received a NACK when communicating with STM
     */
    fun program(stmOtaMessageChannel: StmOtaMessageChannel, errorHandler: ResponseErrorHandler, firmwareHexFile: String): Observable<Float> =
        sendInitBootloaderCommand(stmOtaMessageChannel, errorHandler)
            .andThen(parseFirmwareFile(firmwareHexFile))
            .pairWithProgress()
            .flattenAsObservable { it }
            .concatMap { (chunk, progress) ->
                sendOtaPacket(stmOtaMessageChannel, errorHandler, chunk)
                    .andThen(Observable.just(progress))
            }

    private fun sendInitBootloaderCommand(stmOtaMessageChannel: StmOtaMessageChannel, errorHandler: ResponseErrorHandler): Completable =
        sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
            InitBootloaderCommand()
        ).verifyResponseIsAck()

    private fun parseFirmwareFile(firmwareHexFile: String): Single<List<FirmwareByteChunk>> =
        single {
            try {
                intelHexParser.parse(firmwareHexFile)
            } catch (e: Exception) {
                throw InvalidFirmwareException("Parsing firmware file failed", e)
            }
        }

    private fun Single<out List<FirmwareByteChunk>>.pairWithProgress() =
        map { chunkList ->
            chunkList.mapIndexed { index, chunk ->
                Pair(chunk, (index + 1).toFloat() / chunkList.size.toFloat())
            }
        }

    private fun sendOtaPacket(stmOtaMessageChannel: StmOtaMessageChannel, errorHandler: ResponseErrorHandler, firmwareByteChunk: FirmwareByteChunk): Completable =
        sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
            WriteMemoryStartCommand()
        ).verifyResponseIsAck().andThen(
            sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
                WriteMemoryAddressCommand(firmwareByteChunk.address)
            )
        ).verifyResponseIsAck().andThen(
            sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
                WriteMemoryDataCommand(firmwareByteChunk.data)
            )
        ).verifyResponseIsAck()

    private fun Single<out CommandAcknowledgement>.verifyResponseIsAck(): Completable =
        flatMapCompletable {
            completable {
                if (it.kind != CommandAcknowledgement.Kind.ACK) {
                    throw OtaFailedException("Received NACK response during STM OTA")
                }
            }
        }
}
