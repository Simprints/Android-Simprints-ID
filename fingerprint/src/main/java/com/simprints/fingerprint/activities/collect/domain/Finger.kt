package com.simprints.fingerprint.activities.collect.domain

import android.os.Parcelable
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import kotlinx.android.parcel.Parcelize

/**
 * Constructor for the Finger class for use in the CollectFingerprintsActivity
 * @param id            The ISO number of the finger (0-9)
 * @param priority      The order in which fingers are auto-added (based on research) (0-9)
 * @param order         The order in which fingers appear in the list and the workflow (0-9)
 */
@Parcelize
data class Finger(val id: FingerIdentifier,
                  val priority: Int,
                  val order: Int) : Comparable<Finger>, Parcelable {

    override fun compareTo(other: Finger): Int {
        return this.order - other.order
    }
}
