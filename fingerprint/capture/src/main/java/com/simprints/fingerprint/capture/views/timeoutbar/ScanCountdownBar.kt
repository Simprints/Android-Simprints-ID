package com.simprints.fingerprint.capture.views.timeoutbar

import android.os.CountDownTimer
import android.widget.ProgressBar
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class ScanCountdownBar(
    val progressBar: ProgressBar,
    scanningTimeoutMs: Long,
) : CountDownTimer(scanningTimeoutMs, scanningTimeoutMs / TOTAL_PROGRESS.toLong()) {
    private var scanningProgress = INITIAL_PROGRESS

    fun startTimeoutBar() {
        progressBar.progress = INITIAL_PROGRESS
        scanningProgress = INITIAL_PROGRESS
        start()
    }

    override fun onTick(millisUntilFinished: Long) {
        scanningProgress += PROGRESS_INCREMENT
        progressBar.progress = scanningProgress
    }

    override fun onFinish() {
        progressBar.progress = TOTAL_PROGRESS
    }

    fun handleCancelled() {
        cancel()
        progressBar.progress = INITIAL_PROGRESS
    }

    companion object {
        const val INITIAL_PROGRESS = 0
        const val TOTAL_PROGRESS = 100
        const val PROGRESS_INCREMENT = 1
    }
}
