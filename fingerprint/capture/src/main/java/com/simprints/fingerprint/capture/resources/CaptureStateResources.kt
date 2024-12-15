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
    is CaptureState.ScanProcess.Scanning -> R.string.fingerprint_capture_cancel_button
    is CaptureState.ScanProcess.TransferringImage -> R.string.fingerprint_capture_please_wait
    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.string.fingerprint_capture_rescan

    is CaptureState.ScanProcess.Collected -> if (scanResult.isGoodScan()) {
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
    is CaptureState.ScanProcess.Scanning,
    is CaptureState.ScanProcess.TransferringImage,
    -> R.color.simprints_grey

    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.color.simprints_red_dark

    is CaptureState.ScanProcess.Collected -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.statusBarColor(): Int = when (this) {
    is CaptureState.NotCollected,
    is CaptureState.ScanProcess.Scanning,
    is CaptureState.ScanProcess.TransferringImage,
    -> R.color.simprints_blue

    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.color.simprints_red_dark

    is CaptureState.ScanProcess.Collected -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun CaptureState.resultTextId(): Int = when (this) {
    is CaptureState.NotCollected -> R.string.fingerprint_capture_empty
    is CaptureState.ScanProcess.Scanning -> R.string.fingerprint_capture_empty
    is CaptureState.ScanProcess.TransferringImage -> if (scanResult.isGoodScan()) {
        R.string.fingerprint_capture_good_scan_message
    } else {
        R.string.fingerprint_capture_poor_scan_message
    }

    is CaptureState.Skipped -> R.string.fingerprint_capture_finger_skipped_message
    is CaptureState.ScanProcess.NotDetected -> R.string.fingerprint_capture_no_finger_detected_message
    is CaptureState.ScanProcess.Collected -> if (scanResult.isGoodScan()) {
        R.string.fingerprint_capture_good_scan_message
    } else {
        R.string.fingerprint_capture_poor_scan_message
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@ColorRes
internal fun CaptureState.resultTextColour(): Int = when (this) {
    is CaptureState.NotCollected,
    is CaptureState.ScanProcess.Scanning,
    -> android.R.color.white

    is CaptureState.ScanProcess.TransferringImage -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }

    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.color.simprints_red_dark

    is CaptureState.ScanProcess.Collected -> if (scanResult.isGoodScan()) {
        R.color.simprints_green
    } else {
        R.color.simprints_red_dark
    }
}
