package com.simprints.fingerprintscanner.tools.ota

private const val HEX_CHARS = "0123456789ABCDEF"

fun String.hexStringToIntArray() : IntArray {
    assert(this.length %2 == 0)
    assert(this.matches("[$HEX_CHARS]*".toRegex()))

    val result = IntArray(length / 2)
    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS.indexOf(this[i]);
        val secondIndex = HEX_CHARS.indexOf(this[i + 1]);
        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte().toPositiveInt()
    }
    return result
}

fun Byte.toPositiveInt() = toInt() and 0xFF
