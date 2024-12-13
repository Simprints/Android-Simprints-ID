package com.simprints.fingerprint.infra.scanner.v2.scanner.ota.cypress

import com.simprints.fingerprint.infra.scanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponseType
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.DownloadCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.PrepareDownloadCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.SendImageChunk
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.commands.VerifyImageCommand
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.tools.crc.Crc32Calculator
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.pairWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

/**
 * Conducts OTA for the Cypress module in accordance to
 * https://cypresssemiconductorco.github.io/btsdk-docs/BT-SDK/WICED-Firmware-Upgrade-Library.pdf
 * Pages 7-9
 */
class CypressOtaController @Inject constructor(
    private val crc32Calculator: Crc32Calculator,
) {
    suspend fun program(
        cypressOtaMessageChannel: CypressOtaMessageChannel,
        firmwareBinFile: ByteArray,
    ): Flow<Float> {
        sendPrepareDownloadCommand(cypressOtaMessageChannel)
        sendDownloadCommand(cypressOtaMessageChannel, firmwareBinFile.size)
        val chunks = createFirmwareChunks(firmwareBinFile).pairWithProgress()
        return chunks
            .asFlow()
            .map { (chunk, progress) ->
                sendImageChunk(cypressOtaMessageChannel, chunk)
                progress
            }.onCompletion {
                // verify OTA is OK
                sendVerifyImageCommand(
                    cypressOtaMessageChannel,
                    crc32Calculator.calculateCrc32(firmwareBinFile),
                )
            }
    }

    private suspend fun sendPrepareDownloadCommand(cypressOtaMessageChannel: CypressOtaMessageChannel) = cypressOtaMessageChannel
        .sendCommandAndReceiveResponse<CypressOtaResponse>(PrepareDownloadCommand())
        .verifyResponseIs(CypressOtaResponseType.OK, "PrepareDownloadCommand")

    private suspend fun sendDownloadCommand(
        cypressOtaMessageChannel: CypressOtaMessageChannel,
        imageSize: Int,
    ) = cypressOtaMessageChannel
        .sendCommandAndReceiveResponse<CypressOtaResponse>(DownloadCommand(imageSize))
        .verifyResponseIs(CypressOtaResponseType.OK, "DownloadCommand")

    private suspend fun sendImageChunk(
        cypressOtaMessageChannel: CypressOtaMessageChannel,
        chunk: ByteArray,
    ) = cypressOtaMessageChannel
        .sendCommandAndReceiveResponse<CypressOtaResponse>(SendImageChunk(chunk))
        .verifyResponseIs(CypressOtaResponseType.CONTINUE, "SendImageChunk")

    private suspend fun sendVerifyImageCommand(
        cypressOtaMessageChannel: CypressOtaMessageChannel,
        crc32: Int,
    ) = cypressOtaMessageChannel
        .sendCommandAndReceiveResponse<CypressOtaResponse>(VerifyImageCommand(crc32))
        .verifyResponseIs(CypressOtaResponseType.OK, "VerifyImageCommand")

    private fun createFirmwareChunks(firmwareBinFile: ByteArray): List<ByteArray> {
        // For some unknown reason, the first payload can only be 16 bytes
        if (firmwareBinFile.size < 16) return listOf(firmwareBinFile)
        val firstPayload = firmwareBinFile.toList().subList(0, 16).toByteArray()
        val rest = firmwareBinFile.toList().subList(16, firmwareBinFile.size).toByteArray()
        return listOf(firstPayload) + rest.chunked(CypressOtaMessageProtocol.MAX_PAYLOAD_SIZE)
    }

    private fun CypressOtaResponse.verifyResponseIs(
        type: CypressOtaResponseType,
        commandName: String,
    ) {
        if (this.type != type) {
            throw OtaFailedException("Received unexpected $type response during Cypress OTA in response to $commandName")
        }
    }
}
