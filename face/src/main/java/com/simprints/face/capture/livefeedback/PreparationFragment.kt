package com.simprints.face.capture.livefeedback

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.face.R
import kotlinx.android.synthetic.main.fragment_preparation.*

class PreparationFragment : Fragment(R.layout.fragment_preparation) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setTextInLayout()

        detection_onboarding_frame.setOnClickListener {
            findNavController().navigate(R.id.action_preparationFragment_to_liveFeedbackFragment)
        }
    }

    private fun setTextInLayout() {
        detection_onboarding_light_txt.text = getString(R.string.onboarding_light)
        detection_onboarding_fill_txt.text = getString(R.string.onboarding_fill)
        detection_onboarding_straight_txt.text = getString(R.string.onboarding_straight)
        detection_onboarding_txt_continue.text = getString(R.string.onboarding_continue)
    }
}
