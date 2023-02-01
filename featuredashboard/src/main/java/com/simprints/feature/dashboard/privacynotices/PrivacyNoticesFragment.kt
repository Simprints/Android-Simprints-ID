package com.simprints.feature.dashboard.privacynotices

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentPrivacyNoticesBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

// TODO merge in one module with PrivacyNoticesActivity in id.
@AndroidEntryPoint
internal class PrivacyNoticesFragment : Fragment(R.layout.fragment_privacy_notices) {

    private val viewModel by viewModels<PrivacyNoticesViewModel>()
    private val binding by viewBinding(FragmentPrivacyNoticesBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeLiveData()
        binding.privacyNoticeDownloadButton.setOnClickListener { viewModel.fetchPrivacyNotice() }
        binding.privacyNoticeToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeLiveData() {
        viewModel.privacyNoticeState.observe(viewLifecycleOwner) {
            when (it) {
                is PrivacyNoticeState.Available -> setPrivacyNotice(it.privacyNotice)
                is PrivacyNoticeState.DownloadInProgress -> setDownloadInProgress()
                is PrivacyNoticeState.NotAvailable -> {
                    setNoPrivacyNotice()
                    showToast(getString(IDR.string.long_consent_failed_to_download))
                }
                is PrivacyNoticeState.NotAvailableBecauseBackendMaintenance -> {
                    setNoPrivacyNotice()
                    binding.errorCard.isVisible = true
                    binding.errorTextView.text =
                        if (it.estimatedOutage != null && it.estimatedOutage != 0L) {
                            getString(
                                IDR.string.error_backend_maintenance_with_time_message,
                                TimeUtils.getFormattedEstimatedOutage(it.estimatedOutage)
                            )
                        } else getString(IDR.string.error_backend_maintenance_message)
                }
                is PrivacyNoticeState.NotConnectedToInternet -> {
                    setNoPrivacyNotice()
                    showToast(getString(IDR.string.login_no_network))
                }
            }
        }
    }

    private fun setPrivacyNotice(privacyNotice: String) {
        binding.apply {
            privacyNoticeDownloadButton.isVisible = false
            privacyNoticeHeader.isVisible = false
            privacyNoticeDownloadProgressBar.isVisible = false

            privacyNoticeTextView.isVisible = true
            privacyNoticeTextView.text = privacyNotice
            privacyNoticeTextView.movementMethod = ScrollingMovementMethod()
            errorCard.isVisible = false
        }
    }

    private fun setDownloadInProgress() {
        binding.apply {
            errorCard.isVisible = false
            privacyNoticeDownloadButton.isVisible = false
            privacyNoticeTextView.isVisible = false

            privacyNoticeHeader.isVisible = true
            privacyNoticeHeader.text = getString(IDR.string.long_consent_downloading)

            privacyNoticeDownloadProgressBar.isVisible = true
        }
    }

    private fun setNoPrivacyNotice() {
        binding.apply {
            privacyNoticeTextView.isVisible = false
            privacyNoticeDownloadProgressBar.isVisible = false
            privacyNoticeHeader.isVisible = false
            privacyNoticeDownloadButton.isVisible = true
            errorCard.isVisible = false
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}
