package com.simprints.fingerprint.capture.views.timeoutbar

import android.os.CountDownTimer
import android.widget.ProgressBar
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class ScanningTimeoutBar(
    val progressBar: ProgressBar, private val scanningTimeoutMs: Long
) {

    private lateinit var countDownTimer: CountDownTimer

    private var scanningProgress = INITIAL_PROGRESS

    fun startTimeoutBar() {
        progressBar.progress = INITIAL_PROGRESS
        scanningProgress = INITIAL_PROGRESS
        countDownTimer = createScanningTimer().also { it.start() }
    }


    private fun createScanningTimer(): CountDownTimer {
        val interval = scanningTimeoutMs / TOTAL_PROGRESS.toLong()
        return object : CountDownTimer(scanningTimeoutMs, interval) {
            override fun onTick(millisUntilFinished: Long) {
                scanningProgress += PROGRESS_INCREMENT
                progressBar.progress = scanningProgress
            }

            override fun onFinish() {
                progressBar.progress = TOTAL_PROGRESS
            }
        }
    }

    fun handleCancelled() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        progressBar.progress = INITIAL_PROGRESS
    }


    companion object {
        const val INITIAL_PROGRESS = 0
        const val TOTAL_PROGRESS = 100
        const val PROGRESS_INCREMENT = 1
    }
}
