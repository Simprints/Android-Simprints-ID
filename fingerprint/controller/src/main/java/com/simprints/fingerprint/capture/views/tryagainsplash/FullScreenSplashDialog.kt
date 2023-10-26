package com.simprints.fingerprint.capture.views.tryagainsplash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.simprints.fingerprint.R
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModel.Companion.TRY_DIFFERENT_FINGER_SPLASH_DELAY
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

class FullScreenSplashDialog : DialogFragment(R.layout.activity_splash_screen) {

    override fun getTheme() = IDR.style.Theme_Simprints_Dialog_FullScreen

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(TRY_DIFFERENT_FINGER_SPLASH_DELAY)
            dismiss()
        }
    }
}
