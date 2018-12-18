package com.simprints.id.activities.collectFingerprints

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.simprints.id.R
import com.simprints.id.activities.collectFingerprints.fingers.CollectFingerprintsFingerDisplayHelper

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
