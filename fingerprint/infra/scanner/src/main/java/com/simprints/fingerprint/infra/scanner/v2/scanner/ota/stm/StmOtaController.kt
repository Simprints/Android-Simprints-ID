package com.simprints.fingerprint.infra.scanner.v2.scanner.ota.stm

import com.simprints.fingerprint.infra.scanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.StmOtaMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.EraseMemoryAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.EraseMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.GoAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.GoCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.InitBootloaderCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryAddressCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryDataCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.commands.WriteMemoryStartCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toByteArray
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject

/**
 * Conducts OTA for the STM module in accordance to
 * https://www.st.com/content/ccc/resource/technical/document/application_note/51/5f/03/1e/bd/9b/45/be/CD00264342.pdf/files/CD00264342.pdf/jcr:content/translations/en.CD00264342.pdf
 *
 * While in STM OTA Mode, all bytes are piped directly to the STM which is running custom Simprints
 * bootloader - although the API was kept identical to the stock bootloader linked above.
 *
 * There are some particular memory address involved which reference specific parts in memory on
 * the STM32 - [START_ADDRESS] and [GO_ADDRESS].
 */
class StmOtaController @Inject constructor() {
    /**
     * @throws OtaFailedException if received a NACK when communicating with STM
     */
    suspend fun program(
        stmOtaMessageChannel: StmOtaMessageChannel,
        firmwareBinFile: ByteArray,
    ): Flow<Float> {
        sendInitBootloaderCommand(stmOtaMessageChannel)
        eraseMemory(stmOtaMessageChannel)
        val chunks = createFirmwareChunks(firmwareBinFile)
        return chunks
            .pairWithProgress()
            .asFlow()
            .map { (chunk, progress) ->
                sendOtaPacket(stmOtaMessageChannel, chunk)
                progress
            }.onCompletion { sendGoCommandAndAddress(stmOtaMessageChannel) }
    }

    private fun List<FirmwareByteChunk>.pairWithProgress() = mapIndexed { index, chunk ->
        Pair(chunk, (index + 1).toFloat() / this.size.toFloat())
    }

    private suspend fun sendInitBootloaderCommand(stmOtaMessageChannel: StmOtaMessageChannel) {
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                InitBootloaderCommand(),
            ).verifyResponseIsAck()
    }

    private suspend fun eraseMemory(stmOtaMessageChannel: StmOtaMessageChannel) {
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                EraseMemoryStartCommand(),
            ).verifyResponseIsAck()
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                EraseMemoryAddressCommand(ERASE_ALL_ADDRESS),
            ).verifyResponseIsAck()
    }

    private fun createFirmwareChunks(firmwareBinFile: ByteArray): List<FirmwareByteChunk> = firmwareBinFile
        .chunked(MAX_STM_OTA_CHUNK_SIZE)
        .mapIndexed { idx, chunk ->
            val addressInt = START_ADDRESS + idx * MAX_STM_OTA_CHUNK_SIZE
            val address = addressInt.toByteArray(StmOtaMessageProtocol.byteOrder)
            FirmwareByteChunk(address, chunk)
        }

    private suspend fun sendOtaPacket(
        stmOtaMessageChannel: StmOtaMessageChannel,
        firmwareByteChunk: FirmwareByteChunk,
    ) {
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                WriteMemoryStartCommand(),
            ).verifyResponseIsAck()
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                WriteMemoryAddressCommand(firmwareByteChunk.address),
            ).verifyResponseIsAck()
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(
                WriteMemoryDataCommand(firmwareByteChunk.data),
            ).verifyResponseIsAck()
    }

    private suspend fun sendGoCommandAndAddress(stmOtaMessageChannel: StmOtaMessageChannel) {
        stmOtaMessageChannel
            .sendCommandAndReceiveResponse<CommandAcknowledgement>(GoCommand())
            .verifyResponseIsAck()
        stmOtaMessageChannel.sendStmOtaModeCommand( // The ACK sometimes doesn't make it back before the Cypress module disconnects
            GoAddressCommand(GO_ADDRESS.toByteArray(StmOtaMessageProtocol.byteOrder)),
        )
    }

    private fun CommandAcknowledgement.verifyResponseIsAck() {
        if (this.kind != CommandAcknowledgement.Kind.ACK) {
            throw OtaFailedException("Received NACK response during STM OTA")
        }
    }

    companion object {
        val ERASE_ALL_ADDRESS = byteArrayOf(0xFF, 0xFF)
        const val START_ADDRESS = 0x08004000
        const val GO_ADDRESS = 0x08000000
        const val MAX_STM_OTA_CHUNK_SIZE = 256
    }
}
