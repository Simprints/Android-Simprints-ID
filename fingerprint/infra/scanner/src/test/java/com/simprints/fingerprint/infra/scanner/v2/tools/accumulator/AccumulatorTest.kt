package com.simprints.fingerprint.infra.scanner.v2.tools.accumulator

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AccumulatorTest {
    @Test
    fun accumulator_usedInRxStream_producesCorrectElements() = runTest {
        val fragmentSize = 4
        val strings = listOf("28abcdefghijklmnopqrstuvwxyz", "10DEADBEEF", "05xyz")
        val stringFragments = strings.reduce { acc, s -> acc + s }.chunked(fragmentSize)

        val result = stringFragments.asFlow().accumulateAndTakeElements(StringAccumulator())

        assertThat(result.toList()).containsExactlyElementsIn(strings).inOrder()
    }

    @Test
    fun accumulator_fragmentsAddedDirectly_producesCorrectElements() = runTest {
        val fragmentSize = 4
        val strings = listOf("28abcdefghijklmnopqrstuvwxyz", "10DEADBEEF", "05xyz")
        val stringFragments = strings.reduce { acc, s -> acc + s }.chunked(fragmentSize)

        val accumulator = StringAccumulator()

        stringFragments.forEach { accumulator.updateWithNewFragment(it) }

        val result = accumulator.takeElements()

        assertThat(result.toList()).containsExactlyElementsIn(strings).inOrder()
    }

    @Test
    fun accumulator_resetAfterPartialElement_subsequentElementsAreCorrect() = runTest {
        val accumulator = StringAccumulator()

        // Feed a partial element header that announces a 28-byte element but only supply a few
        // bytes. Without a reset the partial header would corrupt any later element parsing.
        accumulator.updateWithNewFragment("28abc")
        assertThat(accumulator.takeElements().toList()).isEmpty()

        accumulator.reset()

        // After reset, accumulating a fresh complete element should produce exactly that element,
        // with no contamination from the partial bytes received before reset.
        accumulator.updateWithNewFragment("05xyz")
        assertThat(accumulator.takeElements().toList()).containsExactly("05xyz")
    }

    class StringAccumulator :
        Accumulator<String, String, String>(
            initialFragmentCollection = { "" },
            addFragmentToCollection = { this + it },
            canComputeElementLengthFromCollection = { it.length >= LENGTH_INDICES.count() },
            computeElementLengthFromCollection = ::computeLength,
            getCollectionLength = { length },
            sliceCollection = { slice(it) },
            buildElementFromCompleteCollection = { it },
        ) {
        companion object {
            val LENGTH_INDICES = 0..1

            fun computeLength(string: String) = string.slice(LENGTH_INDICES).toInt()
        }
    }
}
