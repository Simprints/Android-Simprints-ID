package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.matcher.FingerprintMatchResult
import org.junit.Test

class MatchResultSetTest {
    @Test
    fun `Stores results sorted descending by confidence up to the limit`() {
        val set = MatchResultSet<FingerprintMatchResult.Item>(3)

        set.add(FingerprintMatchResult.Item("4", 0.4f))
        set.add(FingerprintMatchResult.Item("1", 0.1f))
        set.add(FingerprintMatchResult.Item("3", 0.3f))
        set.add(FingerprintMatchResult.Item("3", 0.1f))
        set.add(FingerprintMatchResult.Item("2", 0.2f))

        assertThat(set.toList()).isEqualTo(
            listOf(
                FingerprintMatchResult.Item("4", 0.4f),
                FingerprintMatchResult.Item("3", 0.3f),
                FingerprintMatchResult.Item("2", 0.2f),
            ),
        )
    }

    @Test
    fun `Merges sets preserving the total limit`() {
        val setOne = MatchResultSet<FingerprintMatchResult.Item>(2)
        setOne.add(FingerprintMatchResult.Item("1", 0.1f))
        setOne.add(FingerprintMatchResult.Item("3", 0.3f))

        val setTwo = MatchResultSet<FingerprintMatchResult.Item>(2)
        setTwo.add(FingerprintMatchResult.Item("2", 0.2f))
        setTwo.add(FingerprintMatchResult.Item("4", 0.4f))

        val set = MatchResultSet<FingerprintMatchResult.Item>(3)
        set.addAll(setOne)
        set.addAll(setTwo)

        assertThat(set.toList()).isEqualTo(
            listOf(
                FingerprintMatchResult.Item("4", 0.4f),
                FingerprintMatchResult.Item("3", 0.3f),
                FingerprintMatchResult.Item("2", 0.2f),
            ),
        )
    }
}
