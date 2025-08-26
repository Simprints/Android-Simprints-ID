package com.simprints.core.tools.extentions

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FlowExtTest {
    @Test
    fun `combine9 combines 9 flows`() = runTest {
        val flow1 = flowOf(1)
        val flow2 = flowOf(2)
        val flow3 = flowOf(3)
        val flow4 = flowOf(4)
        val flow5 = flowOf(5)
        val flow6 = flowOf(6)
        val flow7 = flowOf(7)
        val flow8 = flowOf(8)
        val flow9 = flowOf(9)

        val result = combine9(flow1, flow2, flow3, flow4, flow5, flow6, flow7, flow8, flow9) { t1, t2, t3, t4, t5, t6, t7, t8, t9 ->
            t1 + t2 + t3 + t4 + t5 + t6 + t7 + t8 + t9
        }.toList()

        assertThat(result).isEqualTo(listOf(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9))
    }

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
