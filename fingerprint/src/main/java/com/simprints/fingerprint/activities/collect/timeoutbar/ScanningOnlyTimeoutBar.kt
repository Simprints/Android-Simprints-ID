package com.simprints.fingerprint.activities.collect.timeoutbar

import android.content.Context
import android.os.CountDownTimer
import android.widget.ProgressBar

class ScanningOnlyTimeoutBar(
    override val context: Context,
    override val progressBar: ProgressBar,
    private val scanningTimeoutMs: Long
) : ScanningTimeoutBar {

    private var countDownTimer: CountDownTimer? = null

    private var scanningProgress = 0

    override fun startTimeoutBar() {
        progressBar.progress = 0
        countDownTimer = createScanningTimer().also { it.start() }
    }

    private fun createScanningTimer(): CountDownTimer =
        object : CountDownTimer(scanningTimeoutMs, (scanningTimeoutMs / 100)) {
            override fun onTick(millisUntilFinished: Long) {
                scanningProgress += 1
                progressBar.progress = scanningProgress
            }

            override fun onFinish() {
                progressBar.progress = 100
            }
        }

    override fun handleScanningFinished() {
        handleAllStepsFinished()
    }

    override fun handleAllStepsFinished() {
        countDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
    }

    override fun handleCancelled() {
        countDownTimer?.let {
            it.cancel()
            progressBar.progress = 0
        }
    }
}
