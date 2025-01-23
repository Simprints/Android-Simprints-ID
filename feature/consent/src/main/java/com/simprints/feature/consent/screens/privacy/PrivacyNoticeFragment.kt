package com.simprints.feature.consent.screens.privacy

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.feature.consent.R
import com.simprints.feature.consent.databinding.FragmentPrivacyBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class PrivacyNoticeFragment : Fragment(R.layout.fragment_privacy) {
    private val binding by viewBinding(FragmentPrivacyBinding::bind)
    private val viewModel by viewModels<PrivacyNoticeViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("PrivacyNoticeFragment started", tag = ORCHESTRATION)

        binding.privacyText.movementMethod = ScrollingMovementMethod()
        binding.privacyToolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.privacyDownloadButton.setOnClickListener { viewModel.downloadPressed() }

        observeState()
        viewModel.retrievePrivacyNotice()
    }

    private fun observeState() {
        viewModel.showOffline.observe(viewLifecycleOwner) {
            showToast(IDR.string.login_no_network_error)
        }
        viewModel.viewState.observe(viewLifecycleOwner) {
            when (it) {
                is PrivacyNoticeState.ConsentAvailable -> setConsentAvailable(it)
                is PrivacyNoticeState.ConsentNotAvailable -> setConsentNotAvailable()
                is PrivacyNoticeState.BackendMaintenance -> setBackendMaintenance(it.estimatedOutage)
                is PrivacyNoticeState.DownloadInProgress -> setDownloadProgress()
            }
        }
    }

    private fun showToast(stringRes: Int) {
        Toast.makeText(requireContext(), getString(stringRes), Toast.LENGTH_LONG).show()
    }

    private fun setConsentAvailable(state: PrivacyNoticeState.ConsentAvailable) = with(binding) {
        if (state.consent.isEmpty()) {
            setNoPrivacyNoticeFound()
        } else {
            setLongConsentText(state.consent)
        }
    }

    private fun setNoPrivacyNoticeFound() = with(binding) {
        privacyDownloadButton.isVisible = true

        privacyText.isVisible = false
        privacyProgress.isVisible = false
        privacyHeader.isVisible = false
        privacyErrorCard.isVisible = false
    }

    private fun setLongConsentText(text: String) = with(binding) {
        privacyText.isVisible = true
        privacyText.text = text

        privacyDownloadButton.isVisible = false
        privacyProgress.isVisible = false
        privacyHeader.isVisible = false
        privacyErrorCard.isVisible = false
    }

    private fun setConsentNotAvailable() {
        setNoPrivacyNoticeFound()
        showToast(IDR.string.consent_privacy_notice_failed_to_download)
    }

    private fun setBackendMaintenance(estimatedOutage: String?) = with(binding) {
        privacyDownloadButton.isVisible = true
        privacyErrorText.text = if (estimatedOutage.isNullOrBlank()) {
            getString(IDR.string.error_backend_maintenance_message)
        } else {
            getString(
                IDR.string.error_backend_maintenance_with_time_message,
                estimatedOutage,
            )
        }
        privacyErrorCard.isVisible = true

        privacyText.isVisible = false
        privacyText.text = null
        privacyProgress.isVisible = false
        privacyHeader.isVisible = false
    }

    private fun setDownloadProgress() = with(binding) {
        privacyHeader.isVisible = true
        privacyProgress.isVisible = true

        privacyDownloadButton.isVisible = false
        privacyText.isVisible = false
        privacyErrorCard.isVisible = false
    }
}
