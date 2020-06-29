package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.tools.device.DeviceManager
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_privacy_notice.*
import javax.inject.Inject

class PrivacyNoticeActivity : BaseSplitActivity() {

    @Inject lateinit var viewModelFactory: PrivacyNoticeViewModelFactory
    @Inject lateinit var deviceManager: DeviceManager

    private lateinit var viewModel: PrivacyNoticeViewModel

    private val observerForLongConsentText: Observer<String> = Observer {
        if(it.isNullOrEmpty()) {
            setNoPrivacyNoticeFound()
        } else {
            setLongConsentText(it)
        }
    }

    private val observerForDownloadProgress: Observer<Int> = Observer {
        setDownloadProgress(it)
    }

    private val observerForDownloadSuccess: Observer<Boolean> = Observer {
        if(!it) {
            showDownloadErrorToast()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (application as Application).component.also { it.inject(this) }
        viewModel = ViewModelProvider(this, viewModelFactory).get(PrivacyNoticeViewModel::class.java)

        setContentView(R.layout.activity_privacy_notice)

        initActionBar()
        viewModel.start()

        initInUi()
        observeUi()
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
        longConsent_downloadButton.text = getString(R.string.long_consent_download_button_text)
        longConsent_downloadButton.setOnClickListener {
            if(deviceManager.isConnected()) {
                viewModel.downloadLongConsent()
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
        with(viewModel) {
            longConsentTextLiveData.observe(this@PrivacyNoticeActivity, observerForLongConsentText)
            downloadProgressLiveData.observe(this@PrivacyNoticeActivity, observerForDownloadProgress)
            isDownloadSuccessfulLiveData.observe(this@PrivacyNoticeActivity, observerForDownloadSuccess)
        }
    }

    private fun setLongConsentText(text: String) {
        longConsent_TextView.text = text
        longConsent_TextView.movementMethod = ScrollingMovementMethod()
        longConsent_downloadButton.visibility = View.GONE
        longConsent_noPrivacyNoticeText.visibility = View.GONE
        longConsent_downloadProgressBar.visibility = View.GONE
        longConsent_TextView.visibility = View.VISIBLE
    }

    private fun setNoPrivacyNoticeFound() {
        longConsent_TextView.visibility = View.GONE
        longConsent_downloadButton.visibility = View.VISIBLE
        longConsent_downloadButton.isEnabled = true
        longConsent_noPrivacyNoticeText.visibility = View.VISIBLE
        longConsent_downloadProgressBar.visibility = View.INVISIBLE
    }

    private fun setDownloadProgress(progress: Int) {
        longConsent_downloadButton.isEnabled = false
        hideProgressBarIfNecessary(progress)
        longConsent_downloadProgressBar.progress = progress
    }

    private fun hideProgressBarIfNecessary(progress: Int) {
        if(progress == PROGRESS_COMPLETE) {
            longConsent_downloadProgressBar.visibility = View.INVISIBLE
        } else {
            longConsent_downloadProgressBar.visibility = View.VISIBLE
        }
    }

    private fun showDownloadErrorToast() {
        showToast(R.string.long_consent_failed_to_download)
    }

    private fun showUserOfflineToast() {
        showToast(R.string.login_no_network)
    }

    companion object {
        private const val PROGRESS_COMPLETE = 100
    }
}
