package com.simprints.fingerprint.capture.resources

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.infra.uibase.annotations.ExcludedFromGeneratedTestCoverageReports
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun FingerState.indicatorDrawableId(selected: Boolean): Int =
    if (selected) indicatorSelectedDrawableId() else indicatorDeselectedDrawableId()

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun FingerState.indicatorSelectedDrawableId(): Int = when (this.currentCapture()) {
    is CaptureState.NotCollected,
    is CaptureState.ScanProcess.Scanning,
    is CaptureState.ScanProcess.TransferringImage,
    -> R.drawable.blank_selected

    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.drawable.alert_selected

    is CaptureState.ScanProcess.Collected -> when {
        captures.all { it is CaptureState.ScanProcess.Collected && it.scanResult.isGoodScan() } -> R.drawable.ok_selected
        captures.any { it is CaptureState.NotCollected } -> R.drawable.blank_selected
        else -> R.drawable.alert_selected
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@DrawableRes
internal fun FingerState.indicatorDeselectedDrawableId(): Int = when (this.currentCapture()) {
    is CaptureState.NotCollected,
    is CaptureState.ScanProcess.Scanning,
    is CaptureState.ScanProcess.TransferringImage,
    -> R.drawable.blank_deselected

    is CaptureState.Skipped,
    is CaptureState.ScanProcess.NotDetected,
    -> R.drawable.alert_deselected

    is CaptureState.ScanProcess.Collected -> when {
        captures.all { it is CaptureState.ScanProcess.Collected && it.scanResult.isGoodScan() } -> R.drawable.ok_deselected
        captures.any { it is CaptureState.NotCollected } -> R.drawable.blank_deselected
        else -> R.drawable.alert_deselected
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@Suppress("unused")
@ColorRes
internal fun FingerState.nameTextColour(): Int = IDR.color.simprints_blue

@ExcludedFromGeneratedTestCoverageReports("UI code")
@Suppress("unused")
@StringRes
internal fun FingerState.captureNumberTextId(): Int = IDR.string.fingerprint_capture_capture_number_text

@ExcludedFromGeneratedTestCoverageReports("UI code")
@StringRes
internal fun FingerState.directionTextId(isLastFinger: Boolean): Int = when (val currentCapture = this.currentCapture()) {
    is CaptureState.NotCollected -> if (currentCaptureIndex ==
        0
    ) {
        IDR.string.fingerprint_capture_please_scan
    } else {
        IDR.string.fingerprint_capture_please_scan_again
    }
    is CaptureState.ScanProcess.Scanning -> IDR.string.fingerprint_capture_scanning
    is CaptureState.ScanProcess.TransferringImage -> IDR.string.fingerprint_capture_transfering_data
    is CaptureState.Skipped -> IDR.string.fingerprint_capture_good_scan_direction
    is CaptureState.ScanProcess.NotDetected -> IDR.string.fingerprint_capture_poor_scan_direction
    is CaptureState.ScanProcess.Collected -> if (currentCapture.scanResult.isGoodScan()) {
        if (isLastFinger ||
            currentCaptureIndex + 1 < captures.size
        ) {
            IDR.string.fingerprint_capture_empty
        } else {
            IDR.string.fingerprint_capture_good_scan_direction
        }
    } else {
        IDR.string.fingerprint_capture_poor_scan_direction
    }
}

@ExcludedFromGeneratedTestCoverageReports("UI code")
@Suppress("unused")
@ColorRes
internal fun FingerState.directionTextColour(): Int = IDR.color.simprints_grey
