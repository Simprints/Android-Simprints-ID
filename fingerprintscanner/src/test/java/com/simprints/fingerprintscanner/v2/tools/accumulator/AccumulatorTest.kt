package com.simprints.fingerprintscanner.v2.tools.accumulator

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.awaitCompletionWithNoErrors
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.rxkotlin.toFlowable
import org.junit.Test

class AccumulatorTest {

    @Test
    fun accumulator_usedInRxStream_producesCorrectElements() {
        val fragmentSize = 4
        val strings = listOf("28abcdefghijklmnopqrstuvwxyz", "10DEADBEEF", "05xyz")
        val stringFragments = strings.reduce { acc, s -> acc + s }.chunked(fragmentSize)

        val testSubscriber = stringFragments
            .toFlowable()
            .accumulateAndTakeElements(StringAccumulator())
            .testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values())
            .containsExactlyElementsIn(strings)
            .inOrder()
    }

    @Test
    fun accumulator_fragmentsAddedDirectly_producesCorrectElements() {
        val fragmentSize = 4
        val strings = listOf("28abcdefghijklmnopqrstuvwxyz", "10DEADBEEF", "05xyz")
        val stringFragments = strings.reduce { acc, s -> acc + s }.chunked(fragmentSize)

        val accumulator = StringAccumulator()

        stringFragments.forEach { accumulator.updateWithNewFragment(it) }

        val testSubscriber = accumulator.takeElements().testSubscribe()

        testSubscriber.awaitCompletionWithNoErrors()

        assertThat(testSubscriber.values())
            .containsExactlyElementsIn(strings)
            .inOrder()
    }

    class StringAccumulator : Accumulator<String, String, String>(
        initialFragmentCollection = "",
        addFragmentToCollection = { this + it },
        canComputeElementLengthFromCollection = { it.length >= LENGTH_INDICES.count() },
        computeElementLengthFromCollection = ::computeLength,
        getCollectionLength = { length },
        sliceCollection = { slice(it) },
        buildElementFromCompleteCollection = { it }
    ) {
        companion object {
            val LENGTH_INDICES = 0..1

            fun computeLength(string: String) = string.slice(LENGTH_INDICES).toInt()
        }
    }
}
