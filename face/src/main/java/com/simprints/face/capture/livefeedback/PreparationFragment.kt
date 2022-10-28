package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceOnboardingCompleteEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentPreparationBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * This class represents the screen the user is presented to prepare them on how to capture the face
 */
@AndroidEntryPoint
class PreparationFragment : Fragment(R.layout.fragment_preparation) {
    private val binding by viewBinding(FragmentPreparationBinding::bind)

    @Inject
    lateinit var faceSessionEventsManager: FaceSessionEventsManager

    @Inject
    lateinit var faceTimeHelper: FaceTimeHelper

    private var startTime: Long = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startTime = faceTimeHelper.now()
        setTextInLayout()

        binding.detectionOnboardingFrame.setOnClickListener {
            sendOnboardingEvent()
            findNavController().navigate(R.id.action_preparationFragment_to_liveFeedbackFragment)
        }
    }

    private fun setTextInLayout() {
        binding.apply {
            detectionOnboardingLightTxt.text = getString(R.string.onboarding_light)
            detectionOnboardingFillTxt.text = getString(R.string.onboarding_fill)
            detectionOnboardingStraightTxt.text = getString(R.string.onboarding_straight)
            detectionOnboardingTxtContinue.text = getString(R.string.onboarding_continue)
        }
    }

    private fun sendOnboardingEvent() {
        faceSessionEventsManager.addEventInBackground(
            FaceOnboardingCompleteEvent(startTime, faceTimeHelper.now())
        )
    }
}
