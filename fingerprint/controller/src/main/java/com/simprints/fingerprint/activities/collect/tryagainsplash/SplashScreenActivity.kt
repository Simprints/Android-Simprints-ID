package com.simprints.fingerprint.activities.collect.tryagainsplash

import android.os.Bundle
import android.os.Handler
import com.simprints.core.tools.activity.BaseActivity
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel.Companion.TRY_DIFFERENT_FINGER_SPLASH_DELAY
import com.simprints.fingerprint.databinding.ActivitySplashScreenBinding
import com.simprints.infra.uibase.viewbinding.viewBinding

class SplashScreenActivity : BaseActivity() {
    private val binding by viewBinding(ActivitySplashScreenBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setTextInLayout()

        Handler().postDelayed({
            finish()
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }, TRY_DIFFERENT_FINGER_SPLASH_DELAY)
    }

    private fun setTextInLayout() {
        binding.splashGetReady.text = getString(R.string.get_ready)
        binding.splashTryAnotherFinger.text = getString(R.string.try_another_finger)
    }

    override fun onBackPressed() { }
}
