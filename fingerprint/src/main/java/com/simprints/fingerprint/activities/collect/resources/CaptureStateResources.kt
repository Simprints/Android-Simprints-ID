package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.state.CaptureState

@DrawableRes
fun CaptureState.indicatorDrawableId(selected: Boolean): Int =
    if (selected) indicatorSelectedDrawableId() else indicatorDeselectedDrawableId()

@DrawableRes
fun CaptureState.indicatorSelectedDrawableId(): Int =
    when (this) {
        is CaptureState.NotCollected,
        is CaptureState.Scanning,
        is CaptureState.TransferringImage -> R.drawable.ic_blank_selected
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.drawable.ic_alert_selected
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            R.drawable.ic_ok_selected
        } else {
            R.drawable.ic_alert_selected
        }
    }

@DrawableRes
fun CaptureState.indicatorDeselectedDrawableId(): Int =
    when (this) {
        is CaptureState.NotCollected,
        is CaptureState.Scanning,
        is CaptureState.TransferringImage -> R.drawable.ic_blank_deselected
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.drawable.ic_alert_deselected
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            R.drawable.ic_ok_deselected
        } else {
            R.drawable.ic_alert_deselected
        }
    }

@StringRes
fun CaptureState.buttonTextId(isAskingRescan: Boolean): Int =
    when (this) {
        is CaptureState.NotCollected -> R.string.scan_label
        is CaptureState.Scanning -> R.string.cancel_button
        is CaptureState.TransferringImage -> R.string.please_wait_button
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.string.rescan_label
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            if (isAskingRescan) {
                R.string.rescan_label_question
            } else {
                R.string.good_scan_message
            }
        } else {
            R.string.rescan_label
        }
    }

@Suppress("unused")
@ColorRes
fun CaptureState.buttonTextColour(): Int =
    android.R.color.white

@ColorRes
fun CaptureState.buttonBackgroundColour(): Int =
    when (this) {
        is CaptureState.NotCollected -> R.color.simprints_grey
        is CaptureState.Scanning,
        is CaptureState.TransferringImage -> R.color.simprints_blue
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.color.simprints_red
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }

@StringRes
fun CaptureState.resultTextId(): Int =
    when (this) {
        is CaptureState.NotCollected -> R.string.empty
        is CaptureState.Scanning -> R.string.empty
        is CaptureState.TransferringImage -> if (scanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.poor_scan_message
        }
        is CaptureState.Skipped -> R.string.finger_skipped_message
        is CaptureState.NotDetected -> R.string.no_finger_detected_message
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            R.string.good_scan_message
        } else {
            R.string.poor_scan_message
        }
    }

@ColorRes
fun CaptureState.resultTextColour(): Int =
    when (this) {
        is CaptureState.NotCollected,
        is CaptureState.Scanning -> android.R.color.white
        is CaptureState.TransferringImage -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.color.simprints_red
        is CaptureState.Collected -> if (scanResult.isGoodScan()) {
            R.color.simprints_green
        } else {
            R.color.simprints_red
        }
    }
