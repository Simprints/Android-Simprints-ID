package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.databinding.ActivityPrivacyNoticeBinding
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.showToast
import com.simprints.id.tools.utils.getFormattedEstimatedOutage
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

class PrivacyNoticeActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: PrivacyNoticeViewModelFactory

    @Inject
    lateinit var deviceManager: DeviceManager

    private lateinit var viewModel: PrivacyNoticeViewModel
    private val binding by viewBinding(ActivityPrivacyNoticeBinding::inflate)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as Application).component.also { it.inject(this) }
        viewModel = ViewModelProvider(this, viewModelFactory).get(PrivacyNoticeViewModel::class.java)

        setContentView(binding.root)

        initActionBar()

        initInUi()
        observeUi()

        viewModel.retrievePrivacyNotice()
    }

    private fun initActionBar() {
        setSupportActionBar(binding.longConsentToolbar)

        supportActionBar?.run {
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun observeUi() {
        viewModel.getPrivacyNoticeViewStateLiveData().observe(this, Observer {
            when (it) {
                is PrivacyNoticeViewState.ConsentAvailable -> setConsentAvailable(it)
                is PrivacyNoticeViewState.ConsentNotAvailable -> setConsentNotAvailable()
                is PrivacyNoticeViewState.ConsentNotAvailableBecauseBackendMaintenance -> setConsentNotAvailableBecauseBackendMaintenance(it.estimatedOutage)
                is PrivacyNoticeViewState.DownloadInProgress -> setDownloadProgress()
            }
        })
    }

    private fun setConsentAvailable(consentAvailableState: PrivacyNoticeViewState.ConsentAvailable) {
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
                    IDR.string.error_backend_maintenance_with_time_message, getFormattedEstimatedOutage(
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
