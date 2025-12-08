package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FlowExtTest {
    @Test
    fun `onChange triggers action when comparator returns true`() = runTest {
        val flow = flowOf(1, 2, 2, 3)
        val triggeredValues = mutableListOf<Int>()

        val result = flow
            .onChange({ prev, curr -> prev != curr }) { value ->
                triggeredValues.add(value)
            }.toList()

        assertThat(result).isEqualTo(listOf(1, 2, 2, 3))
        assertThat(triggeredValues).isEqualTo(listOf(2, 3))
    }

    @Test
    fun `onChange does not trigger action when comparator returns false`() = runTest {
        val flow = flowOf(1, 1, 1)
        val triggeredValues = mutableListOf<Int>()

        val result = flow
            .onChange({ prev, curr -> prev != curr }) { value ->
                triggeredValues.add(value)
            }.toList()

        assertThat(result).isEqualTo(listOf(1, 1, 1))
        assertThat(triggeredValues).isEmpty()
    }

    @Test
    fun `windowed creates correct windows with partial=false`() = runTest {
        val flow = flowOf(1, 2, 3, 4, 5)

        val result = flow.windowed(3, partial = false).toList()

        assertThat(result).isEqualTo(
            listOf(
                listOf(1, 2, 3),
                listOf(2, 3, 4),
                listOf(3, 4, 5),
            ),
        )
    }

    @Test
    fun `windowed creates correct windows with partial=true`() = runTest {
        val flow = flowOf(1, 2, 3, 4, 5)

        val result = flow.windowed(3, partial = true).toList()

        assertThat(result).isEqualTo(
            listOf(
                listOf(1),
                listOf(1, 2),
                listOf(1, 2, 3),
                listOf(2, 3, 4),
                listOf(3, 4, 5),
            ),
        )
    }

    @Test
    fun `windowed handles single element flow`() = runTest {
        val flow = flowOf(1)

        val resultPartial = flow.windowed(3, partial = true).toList()
        val resultNonPartial = flow.windowed(3, partial = false).toList()

        assertThat(resultPartial).isEqualTo(listOf(listOf(1)))
        assertThat(resultNonPartial).isEmpty()
    }

    @Test
    fun `windowed handles empty flow`() = runTest {
        val flow = flowOf<Int>()

        val resultPartial = flow.windowed(3, partial = true).toList()
        val resultNonPartial = flow.windowed(3, partial = false).toList()

        assertThat(resultPartial).isEmpty()
        assertThat(resultNonPartial).isEmpty()
    }
}
