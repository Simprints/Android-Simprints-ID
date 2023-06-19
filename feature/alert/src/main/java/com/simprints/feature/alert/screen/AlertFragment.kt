package com.simprints.feature.alert.screen

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.infra.uibase.view.setTextWithFallbacks
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.R
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.databinding.FragmentAlertBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ALERT
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.setResult
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
        Simber.tag(ALERT.name).i("Payload:  ${config.color.name} - ${getPayloadContent(config.payload)}")

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
            Simber.tag(ALERT.name).i("Alert back button clicked")
            setPressedButtonResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, config.payload)
            findNavController().popBackStack()
        }
        config.eventType?.let { vm.saveAlertEvent(it) }
    }

    // Since we do not care about type of the stored value
    // using deprecated `get(): Any?` method should fine
    @Suppress("DEPRECATION")
    private fun getPayloadContent(payload: Bundle) =
        payload.keySet().joinToString { payload.get(it).toString() }

    private fun TextView.setupButton(config: AlertButtonConfig, payload: Bundle) {
        setTextWithFallbacks(config.text, config.textRes)
        setOnClickListener {
            config.resultKey?.let {
                Simber.tag(ALERT.name).i("Alert button clicked: $it")
                setPressedButtonResult(it, payload)
            }

            if (config.closeOnClick) {
                // Close parent activity if back stack is empty after pop
                if (!findNavController().popBackStack()) activity?.finish()
            }
        }
    }

    private fun setPressedButtonResult(key: String, payload: Bundle) {
        findNavController().setResult(this, AlertResult(key, payload))
    }
}
