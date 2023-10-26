package com.simprints.fingerprint.capture.views.timeoutbar

import android.widget.ProgressBar

internal interface ScanningTimeoutBar {

    val progressBar: ProgressBar

    fun startTimeoutBar()

    fun handleAllStepsFinished()

    fun handleCancelled()

    fun handleScanningFinished()

    companion object {
        const val INITIAL_PROGRESS = 0
        const val FINISHED_PROGRESS = 100
        const val PROGRESS_INCREMENT = 1
    }
}
