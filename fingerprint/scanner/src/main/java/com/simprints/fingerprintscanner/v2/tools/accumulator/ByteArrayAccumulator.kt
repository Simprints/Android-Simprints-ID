package com.simprints.fingerprintscanner.v2.tools.accumulator

/**
 * An [Accumulator] that uses a [ByteArray] as the intermediate collection.
 *
 * See [Accumulator] for details on behaviour and usage.
 *
 * @param fragmentAsByteArray how to reduce a [Fragment] to a ByteArray
 * @param buildElement how to subsequently build an [Element] from a ByteArray
 */
abstract class ByteArrayAccumulator<in Fragment, Element>(
    inline val fragmentAsByteArray: (Fragment) -> ByteArray,
    canComputeElementLength: (ByteArray) -> Boolean,
    computeElementLength: (ByteArray) -> Int,
    buildElement: (ByteArray) -> Element
): Accumulator<Fragment, ByteArray, Element>(
    byteArrayOf(),
    { fragment -> this + fragmentAsByteArray(fragment) },
    canComputeElementLength,
    computeElementLength,
    { this.size },
    ByteArray::sliceArray,
    buildElement
)
