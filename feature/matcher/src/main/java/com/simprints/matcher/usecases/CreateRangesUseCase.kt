package com.simprints.matcher.usecases

import javax.inject.Inject

internal class CreateRangesUseCase @Inject constructor() {

    operator fun invoke(max: Int, batchSize: Int): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        var start = 0
        var end = batchSize

        while (start < max) {
            if (end > max) {
                end = max
            }
            ranges.add(start until end)
            start += batchSize
            end += batchSize
        }
        return ranges
    }
}
