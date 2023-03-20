package com.simprints.feature.alert.screen

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.R
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.databinding.FragmentAlertBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class AlertFragment : Fragment(R.layout.fragment_alert) {

    private val args: AlertFragmentArgs by navArgs()
    private val vm by viewModels<AlertViewModel>()
    private val binding by viewBinding(FragmentAlertBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val config = args.alertConfiguration

        binding.root.setBackgroundColor(ResourcesCompat.getColor(
            resources,
            when (config.color) {
                AlertColor.Red -> IDR.color.simprints_red
                AlertColor.Yellow -> IDR.color.simprints_yellow
                AlertColor.Gray -> IDR.color.simprints_grey
                AlertColor.Default -> IDR.color.simprints_blue
            },
            null
        ))
        binding.alertTitle.setTextWithFallbacks(config.title, config.titleRes, IDR.string.alert_title)

        binding.alertImage.setImageResource(config.image)
        binding.alertMessage.setTextWithFallbacks(config.message, config.messageRes)
        if (config.messageIcon != null) {
            binding.alertMessage.setCompoundDrawablesWithIntrinsicBounds(config.messageIcon, 0, 0, 0)
        }

        binding.alertLeftButton.setupButton(config.leftButton, config.payload)
        binding.alertRightButton.isVisible = config.rightButton != null
        if (config.rightButton != null) {
            binding.alertRightButton.setupButton(config.rightButton, config.payload)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            setPressedButtonResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, config.payload)
            findNavController().popBackStack()
        }
        config.eventType?.let { vm.saveAlertEvent(it) }
    }

    private fun TextView.setupButton(config: AlertButtonConfig, payload: Bundle) {
        setTextWithFallbacks(config.text, config.textRes)
        setOnClickListener {
            config.resultKey?.let { setPressedButtonResult(it, payload) }
            if (config.closeOnClick) {
                findNavController().popBackStack()
            }
        }
    }

    private fun setPressedButtonResult(key: String, payload: Bundle) {
        setFragmentResult(AlertContract.ALERT_REQUEST, bundleOf(
            AlertContract.ALERT_BUTTON_PRESSED to key,
            AlertContract.ALERT_PAYLOAD to payload,
        ))
    }

    private fun TextView.setTextWithFallbacks(
        rawText: String?,
        @StringRes textFallback: Int?,
        @StringRes default: Int? = null,
    ) = when {
        rawText != null -> text = rawText
        textFallback != null -> setText(textFallback)
        default != null -> setText(default)
        else -> text = null
    }
}
