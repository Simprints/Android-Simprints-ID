package com.simprints.fingerprint.activities.collect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.simprints.id.R
import com.simprints.fingerprint.activities.collect.fingers.CollectFingerprintsFingerDisplayHelper

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler().postDelayed({
            finish()
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }, CollectFingerprintsFingerDisplayHelper.TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }
}
