package com.simprints.libscanner.tools.ota

class HexRecord(val string: String) {
    var type = 0
    var rawString = ""
    var addr = 0

    private var subString = ""
    private var recordLength = 0
    private var hex = IntArray(21)
    private var hexLength = 0
    private var crc : Byte = 0

    init {
        if (string.isNotBlank()) {
            extractRecord()
            checkRecord()
        }
    }

    private fun extractRecord() {
        rawString = string.trimEnd()
        subString = rawString.substringAfter(":")
        hex = subString.hexStringToIntArray()
        hexLength = hex.size
        recordLength = hex[0]
        addr = (hex[1] * 256) + hex[2]
        type = hex[3]
        crc = hex.sum().toByte()
    }

    private fun checkRecord() {
        assert(rawString[0] == ':')
        assert(hexLength == recordLength + 5)
        assert(type in 0..5)
        assert(crc == 0.toByte())
    }

    companion object {
        const val DATA_RECORD = 0
        const val END_OF_FILE = 1
    }
}