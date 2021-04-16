package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.databinding.ActivityPrivacyNoticeBinding
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.showToast
import javax.inject.Inject

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
            title = getString(R.string.privacy_notice_title)
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
                is PrivacyNoticeViewState.DownloadInProgress -> setDownloadProgress(it)
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

    private fun setDownloadProgress(downloadInProgressState: PrivacyNoticeViewState.DownloadInProgress) {
        setDownloadProgress(downloadInProgressState.progress)
    }

    private fun setLongConsentText(text: String) {
        binding.apply {
            longConsentDownloadButton.isVisible = false
            longConsentHeader.isVisible = false
            longConsentDownloadProgressBar.isVisible = false

            longConsentTextView.isVisible = true
            longConsentTextView.text = text
            longConsentTextView.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun setNoPrivacyNoticeFound() {
        binding.apply {
            longConsentTextView.isVisible = false
            longConsentDownloadProgressBar.isVisible = false
            longConsentHeader.isVisible = false
            longConsentDownloadButton.isVisible = true
        }
    }

    private fun setDownloadProgress(progress: Int) {
        binding.apply {
            longConsentDownloadButton.isVisible = false
            longConsentTextView.isVisible = false

            longConsentHeader.isVisible = true
            longConsentHeader.text = getString(R.string.long_consent_downloading)

            longConsentDownloadProgressBar.isVisible = true
            longConsentDownloadProgressBar.progress = progress
        }
    }

    private fun showDownloadErrorToast() {
        showToast(R.string.long_consent_failed_to_download)
    }

    private fun showUserOfflineToast() {
        showToast(R.string.login_no_network)
    }

}
