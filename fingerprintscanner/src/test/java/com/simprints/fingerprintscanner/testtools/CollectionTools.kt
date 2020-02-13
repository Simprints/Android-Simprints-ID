package com.simprints.fingerprintscanner.testtools

import com.simprints.fingerprintscanner.v2.tools.primitives.stripWhiteSpaceToLowercase
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString
import kotlin.math.ceil

fun List<ByteArray>.toHexStrings() = this.map { it.toHexString() }

fun calculateNumberOfElements(chunkSize: Int, totalSize: Int) =
    ceil(totalSize.toFloat() / chunkSize.toFloat()).toInt()

fun List<String>.reduceString() = reduce { acc, s -> acc + s }

fun List<String>.stripWhiteSpaceToLowercase() = map { it.stripWhiteSpaceToLowercase() }

fun <T> interleave(vararg lists: List<T>): List<T> =
    sequence {
        val iterators = lists.map { it.iterator() }

        while (iterators.any { it.hasNext() }) {
            iterators.forEach { if (it.hasNext()) yield(it.next()) }
        }

    }.toList()
