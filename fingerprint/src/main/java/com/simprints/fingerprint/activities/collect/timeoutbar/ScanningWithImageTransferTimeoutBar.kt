package com.simprints.fingerprint.activities.collect.timeoutbar

import android.content.Context
import android.os.CountDownTimer
import android.widget.ProgressBar

class ScanningWithImageTransferTimeoutBar(
    override val context: Context,
    override val progressBar: ProgressBar,
    private val scanningTimeoutMs: Long,
    private val imageTransferTimeoutMs: Long
) : ScanningTimeoutBar {

    private var scanningCountDownTimer: CountDownTimer? = null
    private var imageTransferCountDownTimer: CountDownTimer? = null

    private var scanningProgress = 0
    private var imageTransferProgress = 0

    override fun startTimeoutBar() {
        progressBar.progress = 0
        scanningCountDownTimer = createScanningTimer().also { it.start() }
        imageTransferCountDownTimer = createImageTransferTimer()
    }

    private fun createScanningTimer(): CountDownTimer =
        object : CountDownTimer(scanningTimeoutMs, (scanningTimeoutMs / 50)) {
            override fun onTick(millisUntilFinished: Long) {
                scanningProgress += 1
                progressBar.progress = scanningProgress
            }

            override fun onFinish() {
                progressBar.progress = 50
            }
        }

    private fun createImageTransferTimer(): CountDownTimer =
        object : CountDownTimer(imageTransferTimeoutMs, (imageTransferTimeoutMs / 50)) {
            override fun onTick(millisUntilFinished: Long) {
                imageTransferProgress += 1
                progressBar.progress = 50 + imageTransferProgress
            }

            override fun onFinish() {
                progressBar.progress = 100
            }
        }

    override fun handleScanningFinished() {
        scanningCountDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
        imageTransferCountDownTimer?.start()
    }

    override fun handleAllStepsFinished() {
        imageTransferCountDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
    }

    override fun handleCancelled() {
        scanningCountDownTimer?.let {
            it.cancel()
            progressBar.progress = 0
        }
        imageTransferCountDownTimer?.let {
            it.cancel()
            progressBar.progress = 0
        }
    }
}
