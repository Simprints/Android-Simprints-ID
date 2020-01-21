package com.simprints.fingerprint.activities.collect.timeoutbar

import android.content.Context
import android.os.CountDownTimer
import android.widget.ProgressBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar.Companion.FINISHED_PROGRESS
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar.Companion.INITIAL_PROGRESS
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar.Companion.PROGRESS_INCREMENT

class ScanningWithImageTransferTimeoutBar(
    override val context: Context,
    override val progressBar: ProgressBar,
    private val scanningTimeoutMs: Long,
    private val imageTransferTimeoutMs: Long
) : ScanningTimeoutBar {

    private var scanningCountDownTimer: CountDownTimer? = null
    private var imageTransferCountDownTimer: CountDownTimer? = null

    private var scanningProgress = INITIAL_PROGRESS
    private var imageTransferProgress = INITIAL_PROGRESS

    override fun startTimeoutBar() {
        progressBar.progress = INITIAL_PROGRESS
        scanningProgress = INITIAL_PROGRESS
        imageTransferProgress = INITIAL_PROGRESS
        scanningCountDownTimer = createScanningTimer().also { it.start() }
        imageTransferCountDownTimer = createImageTransferTimer()
    }

    private fun createScanningTimer(): CountDownTimer =
        object : CountDownTimer(scanningTimeoutMs, (scanningTimeoutMs / SCANNING_STEP_FINISHED_PROGRESS)) {
            override fun onTick(millisUntilFinished: Long) {
                scanningProgress += PROGRESS_INCREMENT
                progressBar.progress = scanningProgress
            }

            override fun onFinish() {
                progressBar.progress = SCANNING_STEP_FINISHED_PROGRESS
            }
        }

    private fun createImageTransferTimer(): CountDownTimer =
        object : CountDownTimer(imageTransferTimeoutMs, (imageTransferTimeoutMs / SCANNING_STEP_FINISHED_PROGRESS)) {
            override fun onTick(millisUntilFinished: Long) {
                imageTransferProgress += PROGRESS_INCREMENT
                progressBar.progress = SCANNING_STEP_FINISHED_PROGRESS + imageTransferProgress
            }

            override fun onFinish() {
                progressBar.progress = FINISHED_PROGRESS
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
        scanningCountDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
        imageTransferCountDownTimer?.let {
            it.cancel()
            it.onFinish()
        }
    }

    override fun handleCancelled() {
        scanningCountDownTimer?.let {
            it.cancel()
            progressBar.progress = INITIAL_PROGRESS
        }
        imageTransferCountDownTimer?.let {
            it.cancel()
            progressBar.progress = INITIAL_PROGRESS
        }
    }

    companion object {
        const val SCANNING_STEP_FINISHED_PROGRESS = 50 // Half-way through the progress bar
    }
}
