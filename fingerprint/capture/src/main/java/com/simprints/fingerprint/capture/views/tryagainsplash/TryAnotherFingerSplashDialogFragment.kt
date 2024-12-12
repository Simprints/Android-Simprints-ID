package com.simprints.fingerprint.capture.views.tryagainsplash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.simprints.fingerprint.capture.R
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class TryAnotherFingerSplashDialogFragment : DialogFragment(R.layout.activity_splash_screen) {
    private val vm: TryAnotherFingerViewModel by viewModels()

    override fun getTheme() = IDR.style.Theme_Simprints_Dialog_FullScreen

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        vm.dismiss.observe(viewLifecycleOwner) {
            if (it) dismissAllowingStateLoss()
        }
    }
}
