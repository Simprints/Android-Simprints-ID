package com.simprints.feature.alert.screen

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.R
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.config.AlertConfiguration
import com.simprints.feature.alert.databinding.FragmentAlertBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ALERT
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.navigation.setResult
import com.simprints.infra.uibase.system.Clipboard
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.view.setTextWithFallbacks
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class AlertFragment : Fragment(R.layout.fragment_alert) {
    private val config: AlertConfiguration by navigationParams()
    private val vm by viewModels<AlertViewModel>()
    private val binding by viewBinding(FragmentAlertBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("AlertFragment started", tag = ORCHESTRATION)

        Simber.i("Alert reason: ${config.appErrorReason}", tag = ALERT)

        binding.root.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                when (config.color) {
                    AlertColor.Red -> IDR.color.simprints_red
                    AlertColor.Yellow -> IDR.color.simprints_yellow
                    AlertColor.Gray -> IDR.color.simprints_grey
                    AlertColor.Default -> IDR.color.simprints_blue
                },
                null,
            ),
        )
        binding.alertTitle.setTextWithFallbacks(config.title, config.titleRes, IDR.string.alert_title_fallback)

        binding.alertImage.setImageResource(config.image)
        binding.alertMessage.setTextWithFallbacks(config.message, config.messageRes)

        config.messageIcon?.let {
            binding.alertMessage.setCompoundDrawablesWithIntrinsicBounds(it, 0, 0, 0)
        }

        binding.alertLeftButton.setupButton(config.leftButton, config.appErrorReason)
        binding.alertRightButton.isVisible = config.rightButton != null
        config.rightButton?.let {
            binding.alertRightButton.setupButton(it, config.appErrorReason)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Simber.i("Alert back button clicked", tag = ALERT)
            setPressedButtonResult(AlertContract.ALERT_BUTTON_PRESSED_BACK, config.appErrorReason)
            findNavController().popBackStack()
        }
        config.eventType?.let { vm.saveAlertEvent(it) }

        binding.alertExportButton.setOnClickListener {
            Simber.i("Alert export button clicked", tag = ALERT)
            Clipboard.copyToClipboard(requireContext(), vm.collectExportData())
            Toast.makeText(requireContext(), IDR.string.alert_export_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun TextView.setupButton(
        config: AlertButtonConfig,
        appErrorReason: AppErrorReason?,
    ) {
        setTextWithFallbacks(config.text, config.textRes)
        setOnClickListener {
            config.resultKey?.let {
                Simber.i("Alert button clicked: $it", tag = ALERT)
                setPressedButtonResult(it, appErrorReason)
            }

            if (config.closeOnClick) {
                // Close parent activity if back stack is empty after pop
                if (!findNavController().popBackStack()) activity?.finish()
            }
        }
    }

    private fun setPressedButtonResult(
        key: String,
        appErrorReason: AppErrorReason?,
    ) {
        findNavController().setResult(this, AlertResult(key, appErrorReason))
    }
}
