package com.simprints.matcher.usecases

import javax.inject.Inject

internal class CreateRangesUseCase @Inject constructor() {
    /**
     * Creates a list of ranges to be used for batch processing.
     * Range size is increased dynamically to ensure that first couple of batches are small
     * to speed up initial reads, then it increases to ensure that the last batches are not too small.
     *
     * For example with minBatchSize = 10, returned batches will be 10, 10, 20, 30, 40, 50, 50 in size (if the total allows).
     */
    operator fun invoke(
        totalCount: Int,
        minBatchSize: Int = DEFAULT_BATCH_SIZE,
    ): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        var index = 1

        var nextBatchSize = minBatchSize
        var start = 0
        var end = nextBatchSize

        while (start < totalCount) {
            if (end > totalCount) {
                end = totalCount
            }
            ranges.add(start..end)
            start = end
            end += nextBatchSize

            // Make sure next batch is increased
            nextBatchSize = minBatchSize + (minBatchSize * index.coerceIn(1, 4))
            index++
        }
        return ranges
    }

    companion object {
        /**
         * Experimentally determined batch size that works well for most cases.
         */
        private const val DEFAULT_BATCH_SIZE = 1000
    }
}
