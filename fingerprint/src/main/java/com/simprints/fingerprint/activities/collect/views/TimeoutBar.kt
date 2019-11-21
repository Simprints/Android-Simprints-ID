package com.simprints.fingerprint.activities.collect.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.widget.ProgressBar

import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.models.FingerStatus

import androidx.core.content.ContextCompat

class TimeoutBar(private val context: Context, private val progressBar: ProgressBar, private val timeoutMs: Int) {
    private var countDownTimer: CountDownTimer? = null

    fun startTimeoutBar() {
        val i = intArrayOf(0, timeoutMs)
        progressBar.progress = i[0]
        countDownTimer = object : CountDownTimer(i[1].toLong(), (i[1] / 100).toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                i[0] += 1
                progressBar.progress = i[0]
            }

            override fun onFinish() {
                progressBar.progress = 100
            }
        }
        countDownTimer!!.start()
    }

    fun stopTimeoutBar() {
        if (countDownTimer == null)
            return

        countDownTimer!!.cancel()
        countDownTimer!!.onFinish()
    }

    fun cancelTimeoutBar() {
        if (countDownTimer == null)
            return

        countDownTimer!!.cancel()
        progressBar.progress = 0
    }

    fun setProgressBar(status: FingerStatus) {
        progressBar.progress = 0

        val drawable: Drawable? = when (status) {
            FingerStatus.NOT_COLLECTED -> ContextCompat.getDrawable(context, R.drawable.timer_progress_bar)
            FingerStatus.GOOD_SCAN -> ContextCompat.getDrawable(context, R.drawable.timer_progress_good)
            FingerStatus.BAD_SCAN, FingerStatus.NO_FINGER_DETECTED -> ContextCompat.getDrawable(context, R.drawable.timer_progress_bad)
            else -> return
        }

        progressBar.progressDrawable = drawable
    }

}
