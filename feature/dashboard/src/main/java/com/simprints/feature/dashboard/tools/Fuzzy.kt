package com.simprints.feature.dashboard.tools

import me.xdrop.fuzzywuzzy.FuzzySearch

private const val DEFAULT_MATCHING_SCORE_THRESHOLD = 50

fun <T> List<T>.fuzzySearch(
    query: String?,
    toStringFunction: (item: T) -> String,
    matchingScoreThreshold: Int = DEFAULT_MATCHING_SCORE_THRESHOLD,
): List<T> = FuzzySearch
    .extractAll(
        query,
        this,
        toStringFunction,
        matchingScoreThreshold,
    ).sortedByDescending { it.score }
    .map { it.referent }
