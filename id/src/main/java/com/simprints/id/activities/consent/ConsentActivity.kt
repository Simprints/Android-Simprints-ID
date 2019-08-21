package com.simprints.id.activities.consent

import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.tools.TimeHelper
import kotlinx.android.synthetic.main.activity_consent.*
import javax.inject.Inject

class ConsentActivity : AppCompatActivity() {

    private lateinit var viewModel: ConsentViewModel
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec
    private lateinit var appRequest: AppRequest

    @Inject lateinit var viewModelFactory: ConsentViewModelFactory
    @Inject lateinit var timeHelper: TimeHelper

    private val consentEvents = MutableLiveData<ConsentEvent>()
    private var startConsentEventTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        val component = (application as Application).component
        component.inject(this)

        startConsentEventTime = timeHelper.now()

        appRequest = intent.extras?.getParcelable(AppRequest.BUNDLE_KEY) ?: throw InvalidAppRequest()

        viewModelFactory.consentEvents = consentEvents

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ConsentViewModel::class.java)
        viewModel.appRequest.postValue(appRequest)

        setupTabs()

        observeGeneralConsentData()
        observeParentalConsentData()
        observeParentalConsentExistance()

        addClickListenerToConsentAccept()
        addClickListenerToConsentDecline()
        addClickListenerToPrivacyNotice()
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

        tabHost.setOnTabChangedListener {
            viewModel.isConsentTabGeneral = (it == GENERAL_CONSENT_TAB_TAG)
        }
    }

    private fun observeGeneralConsentData() {
        viewModel.generalConsentText.observe(this, Observer {
            generalConsentTextView.text = it
        })
    }

    private fun observeParentalConsentData() {
        viewModel.parentalConsentText.observe(this, Observer {
            parentalConsentTextView.text = it
        })
    }

    private fun observeParentalConsentExistance() {
        viewModel.parentalConsentExists.observe(this, Observer {
            if (it) {
                tabHost.addTab(parentalConsentTab)
            }
        })
    }

    private fun addClickListenerToConsentAccept() {
        consentAcceptButton.setOnClickListener {
            consentEvents.postValue(ConsentEvent(startConsentEventTime, timeHelper.now(),
                getCurrentConsentTab(), ConsentEvent.Result.ACCEPTED))
        }
    }

    private fun addClickListenerToConsentDecline() {
        consentDeclineButton.setOnClickListener {
            consentEvents.postValue(ConsentEvent(startConsentEventTime, timeHelper.now(),
                getCurrentConsentTab(), ConsentEvent.Result.DECLINED))
        }
    }

    private fun getCurrentConsentTab() = when(tabHost.currentTabTag) {
        GENERAL_CONSENT_TAB_TAG -> ConsentEvent.Type.INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> ConsentEvent.Type.PARENTAL
        else -> throw Exception()
    }

    private fun addClickListenerToPrivacyNotice() {
        privacyNoticeText.setOnClickListener {
            startPrivacyNoticeActivity()
        }
    }

    private fun startPrivacyNoticeActivity() {
        startActivity(Intent(this, LongConsentActivity::class.java))
    }

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
    }
}
