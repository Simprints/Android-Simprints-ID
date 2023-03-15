package com.simprints.feature.alert

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.alert.databinding.FragmentAlertBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertFragment : Fragment(R.layout.fragment_alert) {

    private val args: AlertFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentAlertBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = args.alertConfiguration

        // TODO better configurability
        // TODO BG color
        binding.alertTitle.text = config.title
        binding.alertImage.setImageResource(config.image)
        binding.alertMessage.text = config.message

        // TODO handle button presses
        binding.alertLeftButton.text = config.leftButtonText
        binding.alertRightButton.text = config.rightButtonText
        binding.alertRightButton.isVisible = config.rightButtonText != null

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // TODO handle back press
            findNavController().navigateUp()
        }
    }

    companion object {

        // TODO replace with better builder
        fun createArgs(
            title: String,
            message: String,
            @DrawableRes image: Int,
            leftButtonText: String,
            rightButtonText: String? = null,
        ): Bundle {
            val config = AlertConfiguration(title, message, image, leftButtonText, rightButtonText)
            return AlertFragmentArgs(config).toBundle()
        }

    }
}
