package com.simprints.fingerprintscanner.v2.scanner.ota.un20

import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.StartOtaCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.VerifyOtaCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.WriteOtaChunkCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.StartOtaResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.VerifyOtaResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.WriteOtaChunkResponse
import com.simprints.fingerprintscanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Calculator
import com.simprints.fingerprintscanner.v2.tools.primitives.chunked
import com.simprints.fingerprintscanner.v2.tools.reactive.completable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import java.util.*

class Un20OtaController(private val crc32Calculator: Crc32Calculator) {

    private inline fun <reified R : Un20Response> sendUn20CommandAndReceiveResponse(
        mainMessageChannel: MainMessageChannel,
        errorHandler: ResponseErrorHandler,
        command: Un20Command
    ): Single<R> =
        mainMessageChannel.sendMainModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(errorHandler)

    fun program(
        mainMessageChannel: MainMessageChannel,
        errorHandler: ResponseErrorHandler,
        firmwareBinFile: ByteArray
    ): Observable<Float> =
        startOta(mainMessageChannel, errorHandler)
            .andThen(
                firmwareBinFile.chunked(MAX_OTA_CHUNK_SIZE)
                    .pairWithProgress()
                    .toObservable()
            )
            .concatMap { (chunk, progress) ->
                writeOtaChunk(mainMessageChannel, errorHandler, chunk)
                    .andThen(Observable.just(progress))
            }
            .concatWith(
                verifyOta(mainMessageChannel, errorHandler, crc32Calculator.calculateCrc32(firmwareBinFile))
            )

    private fun startOta(mainMessageChannel: MainMessageChannel, errorHandler: ResponseErrorHandler): Completable =
        sendUn20CommandAndReceiveResponse<StartOtaResponse>(mainMessageChannel, errorHandler,
            StartOtaCommand(UUID.randomUUID().toString())
        ).verifyResultOk { operationResultCode }

    private fun writeOtaChunk(mainMessageChannel: MainMessageChannel, errorHandler: ResponseErrorHandler, chunk: ByteArray): Completable =
        sendUn20CommandAndReceiveResponse<WriteOtaChunkResponse>(mainMessageChannel, errorHandler,
            WriteOtaChunkCommand(chunk)
        ).verifyResultOk { operationResultCode }

    private fun verifyOta(mainMessageChannel: MainMessageChannel, errorHandler: ResponseErrorHandler, crc32: Int): Completable =
        sendUn20CommandAndReceiveResponse<VerifyOtaResponse>(mainMessageChannel, errorHandler,
            VerifyOtaCommand(crc32)
        ).verifyResultOk { operationResultCode }

    private fun List<ByteArray>.pairWithProgress(): List<Pair<ByteArray, Float>> =
        mapIndexed { index, chunk ->
            Pair(chunk, (index + 1).toFloat() / this.size.toFloat())
        }

    private inline fun <reified R : Un20Response> Single<out R>.verifyResultOk(crossinline resultCode: R.() -> OperationResultCode): Completable =
        flatMapCompletable {
            completable {
                if (it.resultCode() != OperationResultCode.OK) {
                    throw OtaFailedException("Received unexpected response code: ${it.resultCode()} during UN20 OTA inside response ${R::class.java.simpleName}")
                }
            }
        }

    companion object {
        const val MAX_OTA_CHUNK_SIZE = 990
    }
}
