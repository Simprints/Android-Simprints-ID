package com.simprints.fingerprint.capture.resources

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.infra.resources.R
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun CaptureState.buttonTextId(isAskingRescan: Boolean): Int = when (this) {
    is CaptureState.NotCollected -> R.string.fingerprint_capture_scan
    is CaptureState.Scanning -> R.string.fingerprint_capture_cancel_button
    is CaptureState.TransferringImage -> R.string.fingerprint_capture_please_wait
    is CaptureState.Skipped,
    is CaptureState.NotDetected -> R.string.fingerprint_capture_rescan

    is CaptureState.Collected -> if (scanResult.isGoodScan()) {
        if (isAskingRescan) {
            R.string.fingerprint_capture_rescan_question
        } else {
            R.string.fingerprint_capture_good_scan_message
        }
    } else {
        R.string.fingerprint_capture_rescan
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.buttonBackgroundColour(): Int = when (this) {
    is CaptureState.NotCollected -> R.color.simprints_blue
    is CaptureState.Scanning,
    is CaptureState.TransferringImage -> R.color.simprints_grey

    is CaptureState.Skipped,
    is CaptureState.NotDetected -> R.color.simprints_red_dark

    is CaptureState.Collected -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun CaptureState.resultTextId(): Int = when (this) {
    is CaptureState.NotCollected -> R.string.fingerprint_capture_empty
    is CaptureState.Scanning -> R.string.fingerprint_capture_empty
    is CaptureState.TransferringImage -> if (scanResult.isGoodScan()) {
        R.string.fingerprint_capture_good_scan_message
    } else {
        R.string.fingerprint_capture_poor_scan_message
    }

    is CaptureState.Skipped -> R.string.fingerprint_capture_finger_skipped_message
    is CaptureState.NotDetected -> R.string.fingerprint_capture_no_finger_detected_message
    is CaptureState.Collected -> if (scanResult.isGoodScan()) {
        R.string.fingerprint_capture_good_scan_message
    } else {
        R.string.fingerprint_capture_poor_scan_message
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.resultTextColour(): Int = when (this) {
    is CaptureState.NotCollected,
    is CaptureState.Scanning -> android.R.color.white

    is CaptureState.TransferringImage -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }

    is CaptureState.Skipped,
    is CaptureState.NotDetected -> R.color.simprints_red_dark

    is CaptureState.Collected -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }
}
