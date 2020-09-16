package com.simprints.fingerprint.activities.collect.domain

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier.*

class FingerPriorityDeterminer(private val priorityScheme: PriorityScheme = PriorityScheme.DEFAULT) {

    fun determineNextPriorityFinger(existingFingers: List<FingerIdentifier>): FingerIdentifier? =
        priorityScheme.priorities.toMutableList().apply { removeAll(existingFingers) }.firstOrNull()

    interface PriorityScheme {

        /** The priority with which fingers should be auto-added */
        val priorities: List<FingerIdentifier>

        companion object {

            val DEFAULT = object : PriorityScheme {

                override val priorities: List<FingerIdentifier> = listOf(
                    LEFT_THUMB,
                    LEFT_INDEX_FINGER,
                    RIGHT_THUMB,
                    RIGHT_INDEX_FINGER,
                    LEFT_3RD_FINGER,
                    RIGHT_3RD_FINGER,
                    LEFT_4TH_FINGER,
                    RIGHT_4TH_FINGER,
                    LEFT_5TH_FINGER,
                    RIGHT_5TH_FINGER
                )
            }
        }
    }
}
