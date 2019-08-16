package com.simprints.id.activities.consent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import kotlinx.android.synthetic.main.activity_consent.*
import javax.inject.Inject

class ConsentActivity : AppCompatActivity() {

    private lateinit var viewModel: ConsentViewModel
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec
    private lateinit var appRequest: AppRequest

    @Inject lateinit var viewModelFactory: ConsentViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        val component = (application as Application).component
        component.inject(this)

        appRequest = intent.extras?.getParcelable(AppRequest.BUNDLE_KEY) ?: throw InvalidAppRequest()

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ConsentViewModel::class.java)

        setupTabs()

        observeGeneralConsentData()
        observeParentalConsentData()

        addClickListenerToConsentAccept()
        addClickListenerToConsentDecline()

        viewModel.start()
    }

    private fun setupTabs() {
        tabHost.setup()

        generalConsentTab = tabHost.newTabSpec(GENERAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_general_title))
            .setContent(R.id.generalConsentTextView)

        parentalConsentTab = tabHost.newTabSpec(PARENTAL_CONSENT_TAB_TAG)
            .setIndicator(getString(R.string.consent_parental_title))
            .setContent(R.id.parentalConsentTextView)

        tabHost.addTab(generalConsentTab)

        generalConsentTextView.movementMethod = ScrollingMovementMethod()
        parentalConsentTextView.movementMethod = ScrollingMovementMethod()
    }

    private fun observeGeneralConsentData() {
        viewModel.generalConsentData.observe(this, Observer {
            generalConsentTextView.text = it.assembleText(this, appRequest)
        })
    }

    private fun observeParentalConsentData() {
        viewModel.parentalConsentData.observe(this, Observer {
            if (it.parentalConsentExists) {
                tabHost.addTab(parentalConsentTab)
                parentalConsentTextView.text = it.assembleText(this, appRequest)
            }
        })
    }

    private fun addClickListenerToConsentAccept() {
        consentAcceptButton.setOnClickListener { viewModel.handleConsentAcceptClick() }
    }

    private fun addClickListenerToConsentDecline() {
        consentDeclineButton.setOnClickListener { viewModel.handleConsentDeclineClick() }
    }

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
