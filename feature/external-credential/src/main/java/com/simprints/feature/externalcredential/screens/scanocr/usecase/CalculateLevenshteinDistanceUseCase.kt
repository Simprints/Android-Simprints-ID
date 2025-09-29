package com.simprints.feature.externalcredential.screens.scanocr.usecase

import javax.inject.Inject
import kotlin.math.min

internal class CalculateLevenshteinDistanceUseCase @Inject constructor() {

    /**
     * Calculates the Levenshtein distance between two strings.
     *
     * The Levenshtein distance is the minimum number of single-character edits (insertions, deletions, or substitutions) required to change
     * one string into another.
     *
     * Examples:
     * - "kitten" -> "sitting" = 3 (substitute k->s, e->i, insert g)
     * - "ABC" -> "ACD" = 1 (substitute B->C)
     * - "hello" -> "hello" = 0 (identical strings)
     *
     * @param s1 first string
     * @param s2 second string
     * @return minimum number of edits needed to transform s1 into s2
     */
    operator fun invoke(s1: String, s2: String): Int {
        if (s1 == s2) return 0
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1
                    ),
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[s1.length][s2.length]
    }
}
