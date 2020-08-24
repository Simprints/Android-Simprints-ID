package com.simprints.fingerprint.activities.collect.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.FingerState

@DrawableRes
fun FingerState.indicatorDrawableId(selected: Boolean): Int =
    if (selected) indicatorSelectedDrawableId() else indicatorDeselectedDrawableId()

@DrawableRes
fun FingerState.indicatorSelectedDrawableId(): Int =
    when (this.currentCapture()) {
        is CaptureState.NotCollected,
        is CaptureState.Scanning,
        is CaptureState.TransferringImage -> R.drawable.ic_blank_selected
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.drawable.ic_alert_selected
        is CaptureState.Collected -> if (captures.all { it is CaptureState.Collected && it.scanResult.isGoodScan() }) {
            R.drawable.ic_ok_selected
        } else {
            R.drawable.ic_alert_selected
        }
    }

@DrawableRes
fun FingerState.indicatorDeselectedDrawableId(): Int =
    when (this.currentCapture()) {
        is CaptureState.NotCollected,
        is CaptureState.Scanning,
        is CaptureState.TransferringImage -> R.drawable.ic_blank_deselected
        is CaptureState.Skipped,
        is CaptureState.NotDetected -> R.drawable.ic_alert_deselected
        is CaptureState.Collected -> if (captures.all { it is CaptureState.Collected && it.scanResult.isGoodScan() }) {
            R.drawable.ic_ok_deselected
        } else {
            R.drawable.ic_alert_deselected
        }
    }

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
