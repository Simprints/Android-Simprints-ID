package com.simprints.id.activities.longConsent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_long_consent.*
import javax.inject.Inject

class LongConsentActivity : AppCompatActivity(), LongConsentContract.View {

    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper

    override lateinit var viewPresenter: LongConsentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = (application as Application).component.also { it.inject(this) }
        setContentView(R.layout.activity_long_consent)

        initActionBar()
        setTextInLayiout()

        viewPresenter = LongConsentPresenter(this, component)
        viewPresenter.start()
    }

    private fun setTextInLayiout() {
        with(androidResourcesHelper) {
            longConsent_downloadButton.text = getString(R.string.long_consent_download_button_text)
            longConsent_noPrivacyNoticeText.text = getString(R.string.long_consent_no_privacy_notice_found_text)
        }
    }

    private fun initActionBar() {
        setSupportActionBar(longConsentToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setTitle(R.string.privacy_notice_title)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun setLongConsentText(text: String) {
        longConsent_TextView.text = text
        longConsent_TextView.movementMethod = ScrollingMovementMethod()
        longConsent_downloadButton.visibility = View.GONE
        longConsent_noPrivacyNoticeText.visibility = View.GONE
        longConsent_downloadProgressBar.visibility = View.GONE
        longConsent_TextView.visibility = View.VISIBLE
    }

    override fun setNoPrivacyNoticeFound() {
        longConsent_TextView.visibility = View.GONE
        longConsent_downloadButton.visibility = View.VISIBLE
        longConsent_noPrivacyNoticeText.visibility = View.VISIBLE
        longConsent_downloadProgressBar.visibility = View.INVISIBLE
        longConsent_downloadButton.setOnClickListener {
            viewPresenter.downloadLongConsent()
            viewPresenter.logMessageForCrashReportWithUITrigger("Long consent download button clicked")
        }
    }

    override fun setDownloadProgress(progress: Int) {
        longConsent_downloadProgressBar.progress = progress
    }

    override fun setDownloadInProgress(inProgress: Boolean) {
        longConsent_downloadProgressBar.visibility = if (inProgress) View.VISIBLE else View.INVISIBLE
        longConsent_downloadButton.isEnabled = !inProgress
    }

    override fun showDownloadErrorToast() {
        showToast(androidResourcesHelper, R.string.long_consent_failed_to_download)
    }
}
