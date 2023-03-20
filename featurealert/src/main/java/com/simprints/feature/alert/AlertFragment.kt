package com.simprints.feature.alert

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.databinding.FragmentAlertBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class AlertFragment : Fragment(R.layout.fragment_alert) {

    private val args: AlertFragmentArgs by navArgs()
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

        binding.alertLeftButton.setupButton(config.leftButton)
        binding.alertRightButton.isVisible = config.rightButton != null
        if (config.rightButton != null) {
            binding.alertRightButton.setupButton(config.rightButton)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            setPressedButtonResult(ALERT_BUTTON_PRESSED_BACK)
            findNavController().popBackStack()
        }
    }

    private fun TextView.setupButton(config: AlertButtonConfig) {
        setTextWithFallbacks(config.text, config.textRes)
        setOnClickListener {
            config.resultKey?.let { setPressedButtonResult(it) }
            if (config.closeOnClick) {
                findNavController().popBackStack()
            }
        }
    }

    private fun setPressedButtonResult(key: String) {
        setFragmentResult(ALERT_REQUEST, bundleOf(ALERT_BUTTON_PRESSED to key))
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

    companion object {

        const val ALERT_REQUEST = "alert_fragment_request"
        const val ALERT_BUTTON_PRESSED = "alert_fragment_button"
        const val ALERT_BUTTON_PRESSED_BACK = "alert_fragment_back"

        fun hasResponseKey(data: Bundle, key: String) = data.getString(ALERT_BUTTON_PRESSED) == key
    }
}
