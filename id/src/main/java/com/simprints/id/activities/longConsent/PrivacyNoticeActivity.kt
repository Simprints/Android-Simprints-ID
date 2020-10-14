package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_privacy_notice.*
import javax.inject.Inject

class PrivacyNoticeActivity : BaseSplitActivity() {

    @Inject
    lateinit var viewModelFactory: PrivacyNoticeViewModelFactory

    @Inject
    lateinit var deviceManager: DeviceManager

    private lateinit var viewModel: PrivacyNoticeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as Application).component.also { it.inject(this) }
        viewModel = ViewModelProvider(this, viewModelFactory).get(PrivacyNoticeViewModel::class.java)

        setContentView(R.layout.activity_privacy_notice)

        initActionBar()

        initInUi()
        observeUi()

        viewModel.retrievePrivacyNotice()
    }

    private fun initActionBar() {
        setSupportActionBar(longConsentToolbar)

        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.privacy_notice_title)
        }
    }

    private fun initInUi() {
        longConsent_downloadButton.setOnClickListener {
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
        longConsent_downloadButton.isVisible = false
        longConsent_header.isVisible = false
        longConsent_downloadProgressBar.isVisible = false

        longConsent_TextView.isVisible = true
        longConsent_TextView.text = text
        longConsent_TextView.movementMethod = ScrollingMovementMethod()
    }

    private fun setNoPrivacyNoticeFound() {
        longConsent_TextView.isVisible = false
        longConsent_downloadProgressBar.isVisible = false
        longConsent_header.isVisible = false

        longConsent_downloadButton.isVisible = true
    }

    private fun setDownloadProgress(progress: Int) {
        longConsent_downloadButton.isVisible = false
        longConsent_TextView.isVisible = false

        longConsent_header.isVisible = true
        longConsent_header.text = getString(R.string.long_consent_downloading)

        longConsent_downloadProgressBar.isVisible = true
        longConsent_downloadProgressBar.progress = progress
    }

    private fun showDownloadErrorToast() {
        showToast(R.string.long_consent_failed_to_download)
    }

    private fun showUserOfflineToast() {
        showToast(R.string.login_no_network)
    }

}
