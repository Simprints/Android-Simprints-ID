package com.simprints.fingerprint.activities.collect.domain

import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier.*

class FingerOrderDeterminer(private val orderingScheme: OrderingScheme = OrderingScheme.DEFAULT) {

    fun <T> sortedUsingCaptureOrder(list: List<T>, fingerIdSelector: T.() -> FingerIdentifier): List<T> =
        list.sortedBy { t -> orderingScheme.orders.indexOf(t.fingerIdSelector()) }

    fun determineNextPriorityFinger(existingFingers: List<FingerIdentifier>): FingerIdentifier? =
        orderingScheme.priorities.toMutableList().apply { removeAll(existingFingers) }.firstOrNull()

    interface OrderingScheme {
        /** The order in which fingers should appear for collection */
        val orders: List<FingerIdentifier>

        /** The priority with which fingers should be auto-added */
        val priorities: List<FingerIdentifier>

        companion object {

            val DEFAULT = object : OrderingScheme {

                override val orders: List<FingerIdentifier> = listOf(
                    LEFT_THUMB,
                    LEFT_INDEX_FINGER,
                    LEFT_3RD_FINGER,
                    LEFT_4TH_FINGER,
                    LEFT_5TH_FINGER,
                    RIGHT_THUMB,
                    RIGHT_INDEX_FINGER,
                    RIGHT_3RD_FINGER,
                    RIGHT_4TH_FINGER,
                    RIGHT_5TH_FINGER
                )

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
