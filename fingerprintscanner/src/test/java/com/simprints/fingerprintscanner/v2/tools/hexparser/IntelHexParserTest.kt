package com.simprints.fingerprintscanner.v2.tools.hexparser

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.assertThrows
import org.junit.Test

class IntelHexParserTest {

    private val intelHexParser = IntelHexParser()

    @Test
    fun parse() {
        val actualFirmwareByteChunks = intelHexParser.parse(HEX_FILE)
        assertThat(actualFirmwareByteChunks).containsExactlyElementsIn(FIRMWARE_BYTE_CHUNKS).inOrder()
    }

    @Test
    fun parseToRecords() {
        val actualRecords = intelHexParser.parseToRecords(HEX_FILE)
        assertThat(actualRecords).containsExactlyElementsIn(RECORDS).inOrder()
    }

    @Test
    fun validateRecordsConformToExpectedFormat() {
        intelHexParser.validateRecordsConformToExpectedFormat(RECORDS) // Should not throw anything
        assertThrows<IllegalArgumentException> { intelHexParser.validateRecordsConformToExpectedFormat(RECORDS_WITH_UNEXPECTED_RECORD_TYPE) }
        assertThrows<IllegalArgumentException> { intelHexParser.validateRecordsConformToExpectedFormat(RECORDS_WITH_MULTIPLE_END_OF_FILE) }
        assertThrows<IllegalArgumentException> { intelHexParser.validateRecordsConformToExpectedFormat(RECORDS_WITH_END_OF_FILE_IN_WRONG_PLACE) }
        assertThrows<IllegalArgumentException> { intelHexParser.validateRecordsConformToExpectedFormat(RECORDS_WITHOUT_EXTENDED_LINEAR_ADDRESS_RECORD_AT_START) }
    }

    @Test
    fun computeStanzasFromRecords() {
        val actualStanzas = intelHexParser.computeStanzasFromRecords(RECORDS)
        assertThat(actualStanzas).containsExactlyEntriesIn(STANZAS).inOrder()
    }

    @Test
    fun computeFirmwareByteChunksFromStanzas() {
        val actualFirmwareByteChunk = intelHexParser.computeFirmwareByteChunksFromStanzas(STANZAS)
        assertThat(actualFirmwareByteChunk).containsExactlyElementsIn(FIRMWARE_BYTE_CHUNKS).inOrder()
    }

    companion object {

        const val HEX_FILE =
            ":02000004FF00FB" +
                ":10010000214601360121470136007EFE09D2190140" +
                ":100110002146017E17C20001FF5F16002148011928" +
                ":10012000194E79234623965778239EDA3F01B2CAA7" +
                ":02000004FFFFFC" +
                ":100130003F0156702B5E712B722B732146013421C7" +
                ":00000001FF"

        val RECORDS = listOf(
            IntelHexParser.Record(2, "0000".hexToByteArray(), IntelHexParser.RecordType.EXTENDED_LINEAR_ADDRESS, "FF00".hexToByteArray(), 0xFB.toByte()),
            IntelHexParser.Record(16, "0100".hexToByteArray(), IntelHexParser.RecordType.DATA, "214601360121470136007EFE09D21901".hexToByteArray(), 0x40.toByte()),
            IntelHexParser.Record(16, "0110".hexToByteArray(), IntelHexParser.RecordType.DATA, "2146017E17C20001FF5F160021480119".hexToByteArray(), 0x28.toByte()),
            IntelHexParser.Record(16, "0120".hexToByteArray(), IntelHexParser.RecordType.DATA, "194E79234623965778239EDA3F01B2CA".hexToByteArray(), 0xA7.toByte()),
            IntelHexParser.Record(2, "0000".hexToByteArray(), IntelHexParser.RecordType.EXTENDED_LINEAR_ADDRESS, "FFFF".hexToByteArray(), 0xFC.toByte()),
            IntelHexParser.Record(16, "0130".hexToByteArray(), IntelHexParser.RecordType.DATA, "3F0156702B5E712B722B732146013421".hexToByteArray(), 0xC7.toByte()),
            IntelHexParser.Record(0, "0000".hexToByteArray(), IntelHexParser.RecordType.END_OF_FILE, byteArrayOf(), 0xFF.toByte())
        )

        val STANZAS = mapOf(
            IntelHexParser.Record(2, "0000".hexToByteArray(), IntelHexParser.RecordType.EXTENDED_LINEAR_ADDRESS, "FF00".hexToByteArray(), 0xFB.toByte()) to listOf(
                IntelHexParser.Record(16, "0100".hexToByteArray(), IntelHexParser.RecordType.DATA, "214601360121470136007EFE09D21901".hexToByteArray(), 0x40.toByte()),
                IntelHexParser.Record(16, "0110".hexToByteArray(), IntelHexParser.RecordType.DATA, "2146017E17C20001FF5F160021480119".hexToByteArray(), 0x28.toByte()),
                IntelHexParser.Record(16, "0120".hexToByteArray(), IntelHexParser.RecordType.DATA, "194E79234623965778239EDA3F01B2CA".hexToByteArray(), 0xA7.toByte())
            ),
            IntelHexParser.Record(2, "0000".hexToByteArray(), IntelHexParser.RecordType.EXTENDED_LINEAR_ADDRESS, "FFFF".hexToByteArray(), 0xFC.toByte()) to listOf(
                IntelHexParser.Record(16, "0130".hexToByteArray(), IntelHexParser.RecordType.DATA, "3F0156702B5E712B722B732146013421".hexToByteArray(), 0xC7.toByte())
            )
        )

        val FIRMWARE_BYTE_CHUNKS = listOf(
            FirmwareByteChunk("FF000100".hexToByteArray(), "214601360121470136007EFE09D21901".hexToByteArray()),
            FirmwareByteChunk("FF000110".hexToByteArray(), "2146017E17C20001FF5F160021480119".hexToByteArray()),
            FirmwareByteChunk("FF000120".hexToByteArray(), "194E79234623965778239EDA3F01B2CA".hexToByteArray()),
            FirmwareByteChunk("FFFF0130".hexToByteArray(), "3F0156702B5E712B722B732146013421".hexToByteArray())
        )

        val RECORDS_WITH_UNEXPECTED_RECORD_TYPE =
            RECORDS + IntelHexParser.Record(4, "0000".hexToByteArray(), IntelHexParser.RecordType.START_LINEAR_ADDRESS, "000000CD".hexToByteArray(), 0x2A.toByte())

        val RECORDS_WITH_MULTIPLE_END_OF_FILE =
            RECORDS + IntelHexParser.Record(0, "0000".hexToByteArray(), IntelHexParser.RecordType.END_OF_FILE, byteArrayOf(), 0xFF.toByte())

        val RECORDS_WITH_END_OF_FILE_IN_WRONG_PLACE =
            listOf(IntelHexParser.Record(0, "0000".hexToByteArray(), IntelHexParser.RecordType.END_OF_FILE, byteArrayOf(), 0xFF.toByte())) + RECORDS.dropLast(1)

        val RECORDS_WITHOUT_EXTENDED_LINEAR_ADDRESS_RECORD_AT_START =
            RECORDS.drop(1)
    }
}
