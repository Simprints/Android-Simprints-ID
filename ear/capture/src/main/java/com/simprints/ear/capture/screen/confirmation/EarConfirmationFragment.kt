package com.simprints.ear.capture.screen.confirmation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.ear.capture.R
import com.simprints.ear.capture.databinding.FragmentEarConfirmationBinding
import com.simprints.ear.capture.screen.EarCaptureViewModel
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This class represents the screen the user is presented with once they have made a succesful capture
 * of a face
 */
@AndroidEntryPoint
internal class EarConfirmationFragment : Fragment(R.layout.fragment_ear_confirmation) {
    private val binding by viewBinding(FragmentEarConfirmationBinding::bind)

    private val mainVm: EarCaptureViewModel by activityViewModels()

    @Inject
    lateinit var timeHelper: TimeHelper

    private var startTime = Timestamp(0L)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("ConfirmationFragment started", tag = ORCHESTRATION)
        startTime = timeHelper.now()

        binding.apply(::setImageBitmapAndButtonClickListener)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainVm.addCaptureConfirmationAction(startTime, false)
            mainVm.recapture()
        }
    }

    private fun setImageBitmapAndButtonClickListener(binding: FragmentEarConfirmationBinding) {
        mainVm.getSampleDetection()?.bitmap?.let { binding.confirmationImg.setImageBitmap(it) }

        binding.confirmationBtn.setOnClickListener {
            binding.confirmationBtn.setOnClickListener(null)
            mainVm.addCaptureConfirmationAction(startTime, true)
            mainVm.flowFinished()
        }
        binding.recaptureBtn.setOnClickListener {
            mainVm.addCaptureConfirmationAction(startTime, false)
            mainVm.recapture()
        }
    }
}
