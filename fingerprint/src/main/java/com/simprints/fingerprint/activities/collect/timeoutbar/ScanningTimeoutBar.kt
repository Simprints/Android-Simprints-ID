package com.simprints.fingerprint.activities.collect.timeoutbar

import android.content.Context
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.models.FingerStatus

interface ScanningTimeoutBar {

    val context: Context
    val progressBar: ProgressBar

    fun startTimeoutBar()

    fun handleAllStepsFinished()

    fun handleCancelled()

    fun handleScanningFinished()

    fun setProgressBar(status: FingerStatus) {
        progressBar.progress = 0

        progressBar.progressDrawable = when (status) {
            FingerStatus.NOT_COLLECTED -> ContextCompat.getDrawable(context, R.drawable.timer_progress_bar)
            FingerStatus.GOOD_SCAN -> ContextCompat.getDrawable(context, R.drawable.timer_progress_good)
            FingerStatus.BAD_SCAN, FingerStatus.NO_FINGER_DETECTED -> ContextCompat.getDrawable(context, R.drawable.timer_progress_bad)
            else -> return
        }
    }
}
