package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.FingerState

@Suppress("unused")
@ColorRes
fun FingerState.nameTextColour(): Int =
    R.color.simprints_blue

@Suppress("unused")
@StringRes
fun FingerState.captureNumberTextId(): Int =
    R.string.capture_number_text

@StringRes
fun FingerState.directionTextId(isLastFinger: Boolean): Int =
    when (val currentCapture = this.currentCapture()) {
        is CaptureState.NotCollected -> if (currentCaptureIndex == 0) R.string.please_scan else R.string.please_scan_again
        is CaptureState.Scanning -> R.string.scanning
        is CaptureState.TransferringImage -> R.string.transfering_data
        is CaptureState.Skipped -> R.string.good_scan_direction
        is CaptureState.NotDetected -> R.string.poor_scan_direction
        is CaptureState.Collected -> if (currentCapture.scanResult.isGoodScan()) {
            if (isLastFinger || currentCaptureIndex + 1 < captures.size) R.string.empty else R.string.good_scan_direction
        } else {
            R.string.poor_scan_direction
        }
    }

@Suppress("unused")
@ColorRes
fun FingerState.directionTextColour(): Int =
    R.color.simprints_grey
