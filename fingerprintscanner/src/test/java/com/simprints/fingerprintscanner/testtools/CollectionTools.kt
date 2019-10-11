package com.simprints.fingerprintscanner.testtools

import com.simprints.fingerprintscanner.v2.tools.primitives.stripWhiteSpaceToLowercase
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import kotlin.math.ceil

fun ByteArray.chunked(size: Int) =
    this.toList().chunked(size).map { it.toByteArray() }

fun List<ByteArray>.toHexStrings() = this.map { it.toHexString() }

fun calculateNumberOfElements(chunkSize: Int, totalSize: Int) =
    ceil(totalSize.toFloat() / chunkSize.toFloat()).toInt()

fun List<String>.reduceString() = reduce { acc, s -> acc + s }

fun List<String>.stripWhiteSpaceToLowercase() = map { it.stripWhiteSpaceToLowercase() }
