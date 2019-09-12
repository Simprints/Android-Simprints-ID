package com.simprints.fingerprintscanner.v2.tools.accumulator

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
