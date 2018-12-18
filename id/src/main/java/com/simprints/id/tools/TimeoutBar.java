package com.simprints.id.tools;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.widget.ProgressBar;

import com.simprints.id.R;
import com.simprints.id.domain.Finger;

import androidx.core.content.ContextCompat;

public class TimeoutBar {
    private Context context;
    private CountDownTimer countDownTimer;
    private ProgressBar progressBar;
    private int timeoutMs;

    public TimeoutBar(Context context, ProgressBar progressBar, int timeoutMs) {
        this.context = context;
        this.progressBar = progressBar;
        this.timeoutMs = timeoutMs;
    }

    public void startTimeoutBar() {
        final int[] i = {0, timeoutMs};
        progressBar.setProgress(i[0]);
        countDownTimer = new CountDownTimer(i[1], i[1] / 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                i[0] += 1;
                progressBar.setProgress(i[0]);
            }

            @Override
            public void onFinish() {
                progressBar.setProgress(100);
            }
        };
        countDownTimer.start();
    }

    public void stopTimeoutBar() {
        if (countDownTimer == null)
            return;

        countDownTimer.cancel();
        countDownTimer.onFinish();
    }

    public void cancelTimeoutBar() {
        if (countDownTimer == null)
            return;

        countDownTimer.cancel();
        progressBar.setProgress(0);
    }

    public void setProgressBar(Finger.Status status) {
        progressBar.setProgress(0);
        Drawable drawable;

        switch (status) {
            case NOT_COLLECTED:
                drawable = ContextCompat.getDrawable(context,
                        R.drawable.timer_progress_bar);
                break;

            case GOOD_SCAN:
                drawable = ContextCompat.getDrawable(context,
                        R.drawable.timer_progress_good);
                break;

            case BAD_SCAN:
            case NO_FINGER_DETECTED:
                drawable = ContextCompat.getDrawable(context,
                        R.drawable.timer_progress_bad);
                break;

            default:
                return;

        }

        progressBar.setProgressDrawable(drawable);
    }

}
