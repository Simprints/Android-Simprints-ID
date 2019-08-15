package com.simprints.id.activities.consent

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.R
import kotlinx.android.synthetic.main.activity_consent.*

class ConsentActivity : AppCompatActivity() {

    private lateinit var viewModel: ConsentViewModel
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        viewModel = ViewModelProviders.of(this).get(ConsentViewModel::class.java)

        setupTabs()
        observeGeneralConsentText()
        observeParentalConsentText()
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

    private fun observeParentalConsentText() {
        viewModel.parentalConsentText.observe(this, Observer {
            tabHost.addTab(parentalConsentTab)
            parentalConsentTextView.text = it
        })
    }

    private fun observeGeneralConsentText() {
        viewModel.generalConsentText.observe(this, Observer {
            generalConsentTextView.text = it
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
