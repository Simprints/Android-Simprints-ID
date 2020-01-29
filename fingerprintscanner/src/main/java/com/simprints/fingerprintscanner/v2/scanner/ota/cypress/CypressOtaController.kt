package com.simprints.fingerprintscanner.v2.scanner.ota.cypress

import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponseType
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.DownloadCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.PrepareDownloadCommand
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.SendImageChunk
import com.simprints.fingerprintscanner.v2.domain.cypressota.commands.VerifyImageCommand
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Computer
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import com.simprints.fingerprintscanner.v2.tools.reactive.doSimultaneously
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable

class CypressOtaController(private val crc32Computer: Crc32Computer) {

    private inline fun <reified R : CypressOtaResponse> sendCypressOtaModeCommandAndReceiveResponse(
        stmOtaMessageStream: CypressOtaMessageChannel,
        errorHandler: ResponseErrorHandler,
        command: CypressOtaCommand
    ): Single<R> =
        stmOtaMessageStream.outgoing.sendMessage(command)
            .doSimultaneously(stmOtaMessageStream.incoming.receiveResponse<R>())
            .handleErrorsWith(errorHandler)

    fun program(
        cypressOtaMessageChannel: CypressOtaMessageChannel,
        errorHandler: ResponseErrorHandler,
        firmwareBinFile: ByteArray
    ): Observable<Float> =
        sendPrepareDownloadCommand(cypressOtaMessageChannel, errorHandler)
            .andThen(
                sendDownloadCommand(cypressOtaMessageChannel, errorHandler, firmwareBinFile.size)
            )
            .andThen(
                createFirmwareChunks(firmwareBinFile)
                    .pairWithProgress()
                    .toObservable()
            )
            .concatMap { (chunk, progress) ->
                sendImageChunk(cypressOtaMessageChannel, errorHandler, chunk)
                    .andThen(Observable.just(progress))
            }
            .concatWith(
                sendVerifyImageCommand(cypressOtaMessageChannel, errorHandler, crc32Computer.computeCrc32(firmwareBinFile))
            )

    private fun sendPrepareDownloadCommand(cypressOtaMessageChannel: CypressOtaMessageChannel, errorHandler: ResponseErrorHandler): Completable =
        sendCypressOtaModeCommandAndReceiveResponse<CypressOtaResponse>(cypressOtaMessageChannel, errorHandler,
            PrepareDownloadCommand()
        ).verifyResponseIs(CypressOtaResponseType.OK, "PrepareDownloadCommand")

    private fun sendDownloadCommand(cypressOtaMessageChannel: CypressOtaMessageChannel, errorHandler: ResponseErrorHandler, imageSize: Int): Completable =
        sendCypressOtaModeCommandAndReceiveResponse<CypressOtaResponse>(cypressOtaMessageChannel, errorHandler,
            DownloadCommand(imageSize)
        ).verifyResponseIs(CypressOtaResponseType.OK, "DownloadCommand")

    private fun sendImageChunk(cypressOtaMessageChannel: CypressOtaMessageChannel, errorHandler: ResponseErrorHandler, chunk: ByteArray): Completable =
        sendCypressOtaModeCommandAndReceiveResponse<CypressOtaResponse>(cypressOtaMessageChannel, errorHandler,
            SendImageChunk(chunk)
        ).verifyResponseIs(CypressOtaResponseType.CONTINUE, "SendImageChunk")

    private fun sendVerifyImageCommand(cypressOtaMessageChannel: CypressOtaMessageChannel, errorHandler: ResponseErrorHandler, crc32: Int): Completable =
        sendCypressOtaModeCommandAndReceiveResponse<CypressOtaResponse>(cypressOtaMessageChannel, errorHandler,
            VerifyImageCommand(crc32)
        ).verifyResponseIs(CypressOtaResponseType.OK, "VerifyImageCommand")

    private fun createFirmwareChunks(firmwareBinFile: ByteArray): List<ByteArray> {
        // For some unknown reason, the first payload can only be 16 bytes
        if (firmwareBinFile.size < 16) return listOf(firmwareBinFile)
        val firstPayload = firmwareBinFile.toList().subList(0, 16).toByteArray()
        val rest = firmwareBinFile.toList().subList(16, firmwareBinFile.size).toByteArray()
        return listOf(firstPayload) + rest.chunked(CypressOtaMessageProtocol.MAX_PAYLOAD_SIZE)
    }

    private fun List<ByteArray>.pairWithProgress(): List<Pair<ByteArray, Float>> =
        mapIndexed { index, chunk ->
            Pair(chunk, (index + 1).toFloat() / this.size.toFloat())
        }

    private fun Single<out CypressOtaResponse>.verifyResponseIs(type: CypressOtaResponseType, commandName: String): Completable =
        flatMapCompletable {
            completable {
                if (it.type != type) {
                    throw OtaFailedException("Received unexpected $type response during Cypress OTA in response to $commandName")
                }
            }
        }
}
