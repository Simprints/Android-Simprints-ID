package com.simprints.fingerprintscanner.v2.scanner.ota.stm

import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaCommand
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.commands.*
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import com.simprints.fingerprintscanner.v2.tools.reactive.single
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class StmOtaController {

    private inline fun <reified R : StmOtaResponse> sendStmOtaModeCommandAndReceiveResponse(
        stmOtaMessageChannel: StmOtaMessageChannel,
        errorHandler: ResponseErrorHandler,
        command: StmOtaCommand
    ): Single<R> =
        stmOtaMessageChannel.sendStmOtaModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(errorHandler)

    /**
     * @throws OtaFailedException if received a NACK when communicating with STM
     */
    fun program(stmOtaMessageChannel: StmOtaMessageChannel, errorHandler: ResponseErrorHandler, firmwareBinFile: ByteArray): Observable<Float> =
        sendInitBootloaderCommand(stmOtaMessageChannel, errorHandler)
            .andThen(eraseMemory(stmOtaMessageChannel, errorHandler))
            .andThen(calculateFirmwareFileChunks(firmwareBinFile))
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

    private fun eraseMemory(stmOtaMessageChannel: StmOtaMessageChannel, errorHandler: ResponseErrorHandler): Completable =
        sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
            EraseMemoryStartCommand()
        ).verifyResponseIsAck().andThen(
            sendStmOtaModeCommandAndReceiveResponse<CommandAcknowledgement>(stmOtaMessageChannel, errorHandler,
                EraseMemoryAddressCommand(ERASE_ALL_ADDRESS)
            )
        ).verifyResponseIsAck()

    private fun calculateFirmwareFileChunks(firmwareBinFile: ByteArray): Single<List<FirmwareByteChunk>> =
        single {
            firmwareBinFile.chunked(MAX_STM_OTA_CHUNK_SIZE)
                .mapIndexed { idx, chunk ->
                    val addressInt = START_ADDRESS + idx * MAX_STM_OTA_CHUNK_SIZE
                    val address = addressInt.toByteArray(StmOtaMessageProtocol.byteOrder)
                    FirmwareByteChunk(address, chunk)
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

    companion object {
        val ERASE_ALL_ADDRESS = byteArrayOf(0xFF, 0xFF)
        const val START_ADDRESS = 0x08004000
        const val MAX_STM_OTA_CHUNK_SIZE = 256
    }
}
