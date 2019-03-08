package com.simprints.libscanner.tools.ota

class OTAHelper {
    private val PACKET_BIN_SIZE = 256

    fun splitByLines(input: String): List<String> =
            input.split("\n")


    private fun addToNullableString(add: String, line: String?) : String {
        if (line == null) return add + "\n"
        else return line + add + "\n"
    }

    fun splitByPackets(input: String) : Array<String?> {
        val lines = splitByLines(input)
        val noOfRecords = (lines.size-1)
        val records = lines.map{HexRecord(it)}
        val noOfPackets = noOfRecords/16 + 1
        var packets = arrayOfNulls<String>(noOfPackets)
        var refPacketNo = 0
        var currentPacketNo = 0

        fun appendToPacket(string: String, packetNumber: Int) {
            packets[packetNumber] = addToNullableString((string), packets[packetNumber])
        }
        fun isFirstLine() = ((packets[currentPacketNo] == null) && currentPacketNo == 0)
        fun isValidLine(string: String) = (!((string == "\n") or string.isEmpty()))
        fun populatePackets(hex : HexRecord) {
            when (hex.type) {
                HexRecord.DATA_RECORD -> {
                    currentPacketNo = refPacketNo + (hex.addr / PACKET_BIN_SIZE)
                    appendToPacket(hex.rawString, currentPacketNo)
                }
                HexRecord.END_OF_FILE -> {
                    appendToPacket(hex.rawString, currentPacketNo)
                }
                else -> {
                    refPacketNo = if (isFirstLine()) 0 else currentPacketNo + 1
                    appendToPacket(hex.rawString, refPacketNo)
                }
            }
        }

        records.forEach {
            if (isValidLine(it.rawString)) populatePackets(it)
        }
        return packets
    }

    fun getMetaFromFile(input: String) : List<Int> {
        val lines = splitByLines(input).filter { it != "" }
        val linesSplit = lines.map{ it.split("=") }
        return linesSplit.map { it[1].toInt() }
    }

}