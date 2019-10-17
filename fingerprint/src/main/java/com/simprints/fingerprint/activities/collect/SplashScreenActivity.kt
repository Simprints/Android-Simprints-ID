package com.simprints.fingerprint.activities.collect

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.fingers.CollectFingerprintsFingerDisplayHelper
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.koin.android.ext.android.inject

class SplashScreenActivity : AppCompatActivity() {

    val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        setTextInLayout()

        Handler().postDelayed({
            finish()
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }, CollectFingerprintsFingerDisplayHelper.TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }

    private fun setTextInLayout() {
        with(androidResourcesHelper) {
            splashGetReady.text = getString(R.string.get_ready)
            splashTryAnotherFinger.text = getString(R.string.try_another_finger)
        }
    }

    override fun onBackPressed() { }
}
