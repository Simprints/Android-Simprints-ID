package com.simprints.fingerprint.capture.resources

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.infra.resources.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun CaptureState.buttonTextId(isAskingRescan: Boolean): Int =
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

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.buttonBackgroundColour(): Int =
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

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun CaptureState.resultTextId(): Int =
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

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.resultTextColour(): Int =
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
