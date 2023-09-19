package com.simprints.face.capture.screens.confirmation

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.simprints.core.tools.time.TimeHelper
import com.simprints.face.capture.R
import com.simprints.face.capture.databinding.FragmentConfirmationBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.face.capture.screens.FaceCaptureViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This class represents the screen the user is presented with once they have made a succesful capture
 * of a face
 */
@AndroidEntryPoint
internal class ConfirmationFragment : Fragment(R.layout.fragment_confirmation) {

    private val binding by viewBinding(FragmentConfirmationBinding::bind)

    private val mainVm: FaceCaptureViewModel by activityViewModels()

    @Inject
    lateinit var faceTimeHelper: TimeHelper

    private var startTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startTime = faceTimeHelper.now()
        super.onViewCreated(view, savedInstanceState)

        binding.apply(::setImageBitmapAndButtonClickListener)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainVm.addCaptureConfirmationAction(startTime, false)
            mainVm.recapture()
        }
    }

    private fun setImageBitmapAndButtonClickListener(binding: FragmentConfirmationBinding) {
        mainVm.getSampleDetection()?.bitmap?.let { binding.confirmationImg.setImageBitmap(it) }

        binding.confirmationBtn.setOnClickListener {
            mainVm.addCaptureConfirmationAction(startTime, true)
            mainVm.flowFinished()
        }
        binding.recaptureBtn.setOnClickListener {
            mainVm.addCaptureConfirmationAction(startTime, false)
            mainVm.recapture()
        }
    }
}
