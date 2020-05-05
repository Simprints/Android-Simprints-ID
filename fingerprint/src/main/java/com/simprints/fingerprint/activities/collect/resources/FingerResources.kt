package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState

@DrawableRes
fun FingerCollectionState.indicatorSelectedDrawableId(): Int =
    when (this) {
        FingerCollectionState.NotCollected,
        FingerCollectionState.Scanning,
        FingerCollectionState.TransferringImage -> R.drawable.ic_blank_selected
        FingerCollectionState.Skipped,
        FingerCollectionState.NotDetected -> R.drawable.ic_alert_selected
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.drawable.ic_ok_selected
        } else {
            R.drawable.ic_alert_selected
        }
    }

@DrawableRes
fun FingerCollectionState.indicatorDeselectedDrawableId(): Int =
    when (this) {
        FingerCollectionState.NotCollected,
        FingerCollectionState.Scanning,
        FingerCollectionState.TransferringImage -> R.drawable.ic_blank_deselected
        FingerCollectionState.Skipped,
        FingerCollectionState.NotDetected -> R.drawable.ic_alert_deselected
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.drawable.ic_ok_deselected
        } else {
            R.drawable.ic_alert_deselected
        }
    }

@StringRes
fun FingerCollectionState.buttonTextId(): Int =
    when (this) {
        FingerCollectionState.NotCollected -> R.string.scan_label
        FingerCollectionState.Scanning -> R.string.cancel_button
        FingerCollectionState.TransferringImage -> R.string.please_wait_button
        FingerCollectionState.Skipped,
        FingerCollectionState.NotDetected -> R.string.rescan_label
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.rescan_label
        }
    }

@Suppress("unused")
@ColorRes
fun FingerCollectionState.buttonTextColour(): Int =
    android.R.color.white

@ColorRes
fun FingerCollectionState.buttonBackgroundColour(): Int =
    when (this) {
        FingerCollectionState.NotCollected -> R.color.simprints_grey
        FingerCollectionState.Scanning,
        FingerCollectionState.TransferringImage -> R.color.simprints_blue
        FingerCollectionState.Skipped,
        FingerCollectionState.NotDetected -> R.color.simprints_red
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }

@StringRes
fun FingerCollectionState.resultTextId(): Int =
    when (this) {
        FingerCollectionState.NotCollected -> R.string.empty
        FingerCollectionState.Scanning -> R.string.empty
        FingerCollectionState.TransferringImage -> R.string.empty
        FingerCollectionState.Skipped -> R.string.finger_skipped_message
        FingerCollectionState.NotDetected -> R.string.no_finger_detected_message
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.poor_scan_message
        }
    }

@ColorRes
fun FingerCollectionState.resultTextColour(): Int =
    when (this) {
        FingerCollectionState.NotCollected,
        FingerCollectionState.Scanning,
        FingerCollectionState.TransferringImage -> android.R.color.white
        FingerCollectionState.Skipped,
        FingerCollectionState.NotDetected -> R.color.simprints_red
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }

@StringRes
fun FingerCollectionState.directionTextId(isLastFinger: Boolean): Int =
    when (this) {
        FingerCollectionState.NotCollected -> R.string.please_scan
        FingerCollectionState.Scanning -> R.string.scanning
        FingerCollectionState.TransferringImage -> R.string.transfering_data
        FingerCollectionState.Skipped -> R.string.good_scan_direction
        FingerCollectionState.NotDetected -> R.string.poor_scan_direction
        is FingerCollectionState.Collected -> if (fingerScanResult.isGoodScan()) {
            if (isLastFinger) R.string.empty else R.string.good_scan_direction
        } else {
            R.string.poor_scan_direction
        }
    }

@Suppress("unused")
@ColorRes
fun FingerCollectionState.directionTextColour(): Int =
    R.color.simprints_grey
