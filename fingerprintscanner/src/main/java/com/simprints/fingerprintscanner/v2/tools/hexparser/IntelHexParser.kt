package com.simprints.fingerprintscanner.v2.tools.hexparser

import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt

class IntelHexParser {

    fun parse(hexString: String): List<FirmwareByteChunk> {
        val records = parseToRecords(hexString)
        validateRecordsConformToExpectedFormat(records)
        val stanzas = computeStanzasFromRecords(records)
        return computeFirmwareByteChunksFromStanzas(stanzas)
    }

    fun parseToRecords(hexString: String): List<Record> =
        hexString.split(RECORD_SEPARATOR).drop(1)
            .map { it.hexToByteArray() }
            .map { bytes ->
                val dataSize = bytes[DATA_SIZE_INDEX].unsignedToInt()
                Record(
                    dataSize = dataSize,
                    address = bytes.sliceArray(ADDRESS_INDICES),
                    type = RecordType.fromByte(bytes[RECORD_TYPE_INDEX]),
                    data = getDataFromBytes(bytes, dataSize),
                    checkSum = getCheckSumFromBytes(bytes, dataSize)
                )
            }

    /** @throws IllegalArgumentException when the firmware hex record list is not in the expected format */
    fun validateRecordsConformToExpectedFormat(records: List<Record>) {
        //  We are using Intel32Hex, and are only expecting 0x00, 0x01, and 0x04 records
        val expectedRecordTypes = setOf(RecordType.DATA, RecordType.END_OF_FILE, RecordType.EXTENDED_LINEAR_ADDRESS)
        if (records.any { expectedRecordTypes.contains(it.type).not() }) {
            throw IllegalArgumentException("Unexpected record types in firmware hex. " +
                "Expected record types: ${expectedRecordTypes.map { it.toString() }} : " +
                "Record types used : ${records.map { it.type }.toSet()}")
        }

        // There should only be one end of file record, and it should be at the end
        if (records.count { it.type == RecordType.END_OF_FILE } != 1 ||
            records.last().type != RecordType.END_OF_FILE) {
            throw IllegalArgumentException("Unexpected/missing end of file record in firmware hex")
        }

        // The first record should be an extended linear address record
        if (records.first().type != RecordType.EXTENDED_LINEAR_ADDRESS) {
            throw IllegalArgumentException("First record should be extended linear address record")
        }
    }

    /**
     * A "stanza" here refers to a group of records that start with a [RecordType.EXTENDED_LINEAR_ADDRESS]
     * record, and all the following [RecordType.DATA] records that are relative to this address.
     * A stanza ends at the next [RecordType.EXTENDED_LINEAR_ADDRESS] record, or until the
     * [RecordType.END_OF_FILE]
     */
    fun computeStanzasFromRecords(records: List<Record>): Map<Record, List<Record>> {
        // Find all address records and pair with index
        val addressRecordsIndexed = records.withIndex().filter { it.value.type == RecordType.EXTENDED_LINEAR_ADDRESS }

        // Find stanza ranges, i.e. the address record paired with the IntRange of data records in the records list
        val allStanzaRangesExceptLast = addressRecordsIndexed.zipWithNext { a, b -> Pair(a.value, a.index + 1 until b.index) }
        val lastStanzaRange = listOf(Pair(addressRecordsIndexed.last().value, addressRecordsIndexed.last().index + 1 until records.size - 1))

        val stanzaRanges = allStanzaRangesExceptLast + lastStanzaRange

        return stanzaRanges.map { (addressRecord, range) ->
            Pair(addressRecord, records.slice(range))
        }.toMap()
    }

    fun computeFirmwareByteChunksFromStanzas(stanzas: Map<Record, List<Record>>): List<FirmwareByteChunk> =
        stanzas.flatMap { (addressRecord, dataRecords) ->
            val majorAddressBytes = addressRecord.data
            dataRecords.map { dataRecord ->
                val minorAddressBytes = dataRecord.address
                val address = majorAddressBytes + minorAddressBytes
                FirmwareByteChunk(address, dataRecord.data)
            }
        }


    @Suppress("unused")
    data class Record(
        val dataSize: Int,
        val address: ByteArray,
        val type: RecordType,
        val data: ByteArray,
        val checkSum: Byte
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Record

            if (dataSize != other.dataSize) return false
            if (!address.contentEquals(other.address)) return false
            if (type != other.type) return false
            if (!data.contentEquals(other.data)) return false
            if (checkSum != other.checkSum) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataSize
            result = 31 * result + address.contentHashCode()
            result = 31 * result + type.hashCode()
            result = 31 * result + data.contentHashCode()
            result = 31 * result + checkSum
            return result
        }
    }

    @Suppress("unused")
    enum class RecordType(val value: Byte) {
        DATA(0x00),
        END_OF_FILE(0x01),
        EXTENDED_SEGMENT_ADDRESS(0x02),
        START_SEGMENT_ADDRESS(0x03),
        EXTENDED_LINEAR_ADDRESS(0x04),
        START_LINEAR_ADDRESS(0x05);

        companion object {
            fun fromByte(byte: Byte) = values().find { it.value == byte }
                ?: throw IllegalArgumentException("Unexpected byte for Intel HEX record type : $byte")
        }
    }

    companion object {
        private const val RECORD_SEPARATOR = ':'
        private const val DATA_SIZE_INDEX = 0
        private val ADDRESS_INDICES = 1..2
        private const val RECORD_TYPE_INDEX = 3

        private fun getDataFromBytes(rawBytes: ByteArray, dataSize: Int) =
            rawBytes.sliceArray(4 until 4 + dataSize)

        private fun getCheckSumFromBytes(rawBytes: ByteArray, dataSize: Int) =
            rawBytes[4 + dataSize]
    }
}
