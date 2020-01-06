package com.simprints.id.tools.extensions

import me.xdrop.fuzzywuzzy.FuzzySearch

private const val DEFAULT_MATCHING_SCORE_THRESHOLD = 50

fun <T> List<T>.fuzzySearch(
    query: String?,
    toStringFunction: (item: T) -> String,
    matchingScoreThreshold: Int = DEFAULT_MATCHING_SCORE_THRESHOLD
): List<T> {
    return FuzzySearch.extractAll(
        query, this, toStringFunction, matchingScoreThreshold
    ).sortedByDescending { it.score }.map { it.referent }
}
