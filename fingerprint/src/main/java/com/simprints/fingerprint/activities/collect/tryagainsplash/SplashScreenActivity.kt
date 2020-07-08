package com.simprints.fingerprint.activities.collect.tryagainsplash

import android.os.Bundle
import android.os.Handler
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel.Companion.TRY_DIFFERENT_FINGER_SPLASH_DELAY
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : BaseSplitActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        setTextInLayout()

        Handler().postDelayed({
            finish()
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }, TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }

    private fun setTextInLayout() {
        splashGetReady.text = getString(R.string.get_ready)
        splashTryAnotherFinger.text = getString(R.string.try_another_finger)

    }

    override fun onBackPressed() { }
}
