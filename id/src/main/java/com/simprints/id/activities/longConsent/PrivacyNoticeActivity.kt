package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.utils.TimeUtils.getFormattedEstimatedOutage
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.databinding.ActivityPrivacyNoticeBinding
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class PrivacyNoticeActivity : BaseSplitActivity() {

    @Inject
    lateinit var deviceManager: DeviceManager

    private val viewModel: PrivacyNoticeViewModel by viewModels()
    private val binding by viewBinding(ActivityPrivacyNoticeBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        initActionBar()

        initInUi()
        observeUi()

        viewModel.retrievePrivacyNotice()
    }

    private fun initActionBar() {
        setActionBar(binding.longConsentToolbar)
        actionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(IDR.string.privacy_notice_title)
        }
    }

    private fun initInUi() {
        binding.longConsentDownloadButton.setOnClickListener {
            if (deviceManager.isConnected()) {
                viewModel.retrievePrivacyNotice()
            } else {
                showUserOfflineToast()
            }
        }
    }

    override fun onNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun observeUi() {
        viewModel.getPrivacyNoticeViewStateLiveData().observe(this) {
            when (it) {
                is PrivacyNoticeState.ConsentAvailable -> setConsentAvailable(it)
                is PrivacyNoticeState.ConsentNotAvailable -> setConsentNotAvailable()
                is PrivacyNoticeState.ConsentNotAvailableBecauseBackendMaintenance -> setConsentNotAvailableBecauseBackendMaintenance(
                    it.estimatedOutage
                )
                is PrivacyNoticeState.DownloadInProgress -> setDownloadProgress()
            }
        }
    }

    private fun setConsentAvailable(consentAvailableState: PrivacyNoticeState.ConsentAvailable) {
        val consent = consentAvailableState.consent
        if (consent.isEmpty()) {
            setNoPrivacyNoticeFound()
        } else {
            setLongConsentText(consent)
        }
    }

    private fun setConsentNotAvailable() {
        setNoPrivacyNoticeFound()
        showDownloadErrorToast()
    }

    private fun setConsentNotAvailableBecauseBackendMaintenance(estimatedOutage: Long?) {
        binding.apply {
            longConsentTextView.isVisible = false
            longConsentDownloadProgressBar.isVisible = false
            longConsentHeader.isVisible = false
            longConsentDownloadButton.isVisible = true
            errorCard.isVisible = true

            errorTextView.text = if (estimatedOutage != null && estimatedOutage != 0L) {
                getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    getFormattedEstimatedOutage(
                        estimatedOutage
                    )
                )
            } else getString(IDR.string.error_backend_maintenance_message)
        }
    }

    private fun setLongConsentText(text: String) {
        binding.apply {
            longConsentDownloadButton.isVisible = false
            longConsentHeader.isVisible = false
            longConsentDownloadProgressBar.isVisible = false

            longConsentTextView.isVisible = true
            longConsentTextView.text = text
            longConsentTextView.movementMethod = ScrollingMovementMethod()
            errorCard.isVisible = false
        }
    }

    private fun setNoPrivacyNoticeFound() {
        binding.apply {
            longConsentTextView.isVisible = false
            longConsentDownloadProgressBar.isVisible = false
            longConsentHeader.isVisible = false
            longConsentDownloadButton.isVisible = true
            errorCard.isVisible = false
        }
    }


    private fun setDownloadProgress() {
        binding.apply {
            errorCard.isVisible = false
            longConsentDownloadButton.isVisible = false
            longConsentTextView.isVisible = false

            longConsentHeader.isVisible = true
            longConsentHeader.text = getString(IDR.string.long_consent_downloading)

            longConsentDownloadProgressBar.isVisible = true
        }
    }

    private fun showDownloadErrorToast() {
        showToast(IDR.string.long_consent_failed_to_download)
    }

    private fun showUserOfflineToast() {
        showToast(IDR.string.login_no_network)
    }

}
