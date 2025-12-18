package com.simprints.infra.matching.usecase

import com.simprints.core.AvailableProcessors
import javax.inject.Inject
import kotlin.math.ceil

class CreateRangesUseCase @Inject constructor(
    @param:AvailableProcessors private val availableProcessors: Int,
) {
    /**
     * Creates a list of ranges to be used for batch processing.
     * The number of ranges will be a multiple of the available processors to ensure
     * efficient parallel processing.
     * Range sizes are adjusted to not exceed MAX_BATCH_SIZE.
     */
    operator fun invoke(
        totalCount: Int,
    ): List<IntRange> {
        if (totalCount <= 0) return emptyList()

        // Calculate how many multiples of processors we need to keep batches under MAX_BATCH_SIZE
        val batchesPerProcessor = ceil(totalCount.toDouble() / (availableProcessors * MAX_BATCH_SIZE)).toInt().coerceAtLeast(1)
        val totalBatches = availableProcessors * batchesPerProcessor

        // Calculate the base batch size and remainder for even distribution
        val baseBatchSize = totalCount / totalBatches
        val remainder = totalCount % totalBatches

        val ranges = mutableListOf<IntRange>()
        var start = 0

        // Create ranges with sizes distributed as evenly as possible
        for (i in 0 until totalBatches) {
            // Add 1 to batch size for the first 'remainder' batches to distribute the remainder evenly
            val batchSize = baseBatchSize + if (i < remainder) 1 else 0
            val end = (start + batchSize).coerceAtMost(totalCount)

            ranges.add(start until end)
            start = end

            if (start >= totalCount) break
        }

        return ranges
    }

    companion object {
        /**
         * Maximum size for a batch to avoid huge memory consumption
         */
        private const val MAX_BATCH_SIZE = 2000
    }
}
