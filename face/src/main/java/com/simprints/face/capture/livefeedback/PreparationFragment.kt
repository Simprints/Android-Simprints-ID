package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.face.R
import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.model.FaceOnboardingCompleteEvent
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.databinding.FragmentPreparationBinding
import org.koin.android.ext.android.inject

class PreparationFragment : Fragment() {
    private var binding: FragmentPreparationBinding? = null

    private val faceSessionEventsManager: FaceSessionEventsManager by inject()
    private val faceTimeHelper: FaceTimeHelper by inject()
    private val startTime = faceTimeHelper.now()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPreparationBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setTextInLayout()

        binding?.detectionOnboardingFrame?.setOnClickListener {
            sendOnboardingEvent()
            findNavController().navigate(R.id.action_preparationFragment_to_liveFeedbackFragment)
        }
    }

    private fun setTextInLayout() {
        binding?.apply {
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
