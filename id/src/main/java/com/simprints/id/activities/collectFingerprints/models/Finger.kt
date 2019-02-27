package com.simprints.id.activities.collectFingerprints.models

import android.os.Parcelable
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import kotlinx.android.parcel.Parcelize

/**
 * Constructor for the Finger class
 * @param id            The ISO number of the finger (0-9)
 * @param isActive      Whether the finger is the current finger
 * @param priority      The order in which fingers get auto-added (based on research) (0-9)
 * @param order         The order in which fingers appear in the list and the workflow (0-9)
 */
@Parcelize
data class Finger(val id: FingerIdentifier,
                  var isActive: Boolean,
                  val priority: Int,
                  private val order: Int,
                  var status: FingerStatus = FingerStatus.NOT_COLLECTED,
                  var template: Fingerprint? = null,
                  var isLastFinger: Boolean = false,
                  var numberOfBadScans: Int = 0) : Comparable<Finger>, Parcelable {

    companion object {
        val NB_OF_FINGERS = 10
    }

    val isGoodScan: Boolean
        get() = status == FingerStatus.GOOD_SCAN

    val isBadScan: Boolean
        get() = status == FingerStatus.BAD_SCAN

    val isRescanGoodScan: Boolean
        get() = status == FingerStatus.RESCAN_GOOD_SCAN

    val isCollecting: Boolean
        get() = status == FingerStatus.COLLECTING

    val isNotCollected: Boolean
        get() = status == FingerStatus.NOT_COLLECTED

    val isNoFingerDetected: Boolean
        get() = status == FingerStatus.NO_FINGER_DETECTED

    val isFingerSkipped: Boolean
        get() = status == FingerStatus.FINGER_SKIPPED

    init {
        this.status = FingerStatus.NOT_COLLECTED
        this.template = null
        this.isLastFinger = false
        this.numberOfBadScans = 0
    }

    override fun compareTo(other: Finger): Int {
        return this.order - other.order
    }
}
