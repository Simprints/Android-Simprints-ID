package com.simprints.fingerprint.infra.scanner.v2.scanner.ota.un20

import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.StartOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.VerifyOtaCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.WriteOtaChunkCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.StartOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.VerifyOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.WriteOtaChunkResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.tools.crc.Crc32Calculator
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.pairWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import java.util.UUID
import javax.inject.Inject

class Un20OtaController @Inject constructor(
    private val crc32Calculator: Crc32Calculator,
) {
    suspend fun program(
        mainMessageChannel: MainMessageChannel,
        firmwareBinFile: ByteArray,
    ): Flow<Float> {
        startOta(mainMessageChannel)
        val chunks = firmwareBinFile.chunked(MAX_UN20_OTA_CHUNK_SIZE)
        return chunks
            .pairWithProgress()
            .asFlow()
            .map { (chunk, progress) ->
                writeOtaChunk(mainMessageChannel, chunk)
                progress
            }.onCompletion {
                verifyOta(mainMessageChannel, crc32Calculator.calculateCrc32(firmwareBinFile))
            }
    }

    private suspend fun startOta(mainMessageChannel: MainMessageChannel) = mainMessageChannel
        .sendCommandAndReceiveResponse<StartOtaResponse>(
            StartOtaCommand(UUID.randomUUID().toString()),
        ).verifyResultOk()

    private suspend fun writeOtaChunk(
        mainMessageChannel: MainMessageChannel,
        chunk: ByteArray,
    ) = mainMessageChannel
        .sendCommandAndReceiveResponse<WriteOtaChunkResponse>(
            WriteOtaChunkCommand(chunk),
        ).verifyResultOk()

    private suspend fun verifyOta(
        mainMessageChannel: MainMessageChannel,
        crc32: Int,
    ) = mainMessageChannel
        .sendCommandAndReceiveResponse<VerifyOtaResponse>(
            VerifyOtaCommand(crc32),
        ).verifyResultOk()

    private fun StartOtaResponse.verifyResultOk() {
        if (operationResultCode != OperationResultCode.OK) {
            throw OtaFailedException(
                "Received unexpected response code: $operationResultCode during UN20 OTA inside response ${this::class.java.simpleName}",
            )
        }
    }

    private fun WriteOtaChunkResponse.verifyResultOk() {
        if (operationResultCode != OperationResultCode.OK) {
            throw OtaFailedException(
                "Received unexpected response code: $operationResultCode during UN20 OTA inside response ${this::class.java.simpleName}",
            )
        }
    }

    private fun VerifyOtaResponse.verifyResultOk() {
        if (operationResultCode != OperationResultCode.OK) {
            throw OtaFailedException(
                "Received unexpected response code: $operationResultCode during UN20 OTA inside response ${this::class.java.simpleName}",
            )
        }
    }

    companion object {
        const val MAX_UN20_OTA_CHUNK_SIZE = 894
    }
}
