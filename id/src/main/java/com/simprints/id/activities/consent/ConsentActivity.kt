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
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Result.ACCEPTED
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Result.DECLINED
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.INDIVIDUAL
import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent.Type.PARENTAL
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
        setupObserversForUi()
        setupClickListeners()
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

    private fun setupObserversForUi() {
        observeGeneralConsentData()
        observeParentalConsentData()
        observeParentalConsentExistence()
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

    private fun observeParentalConsentExistence() {
        viewModel.parentalConsentExists.observe(this, Observer {
            if (it) {
                tabHost.addTab(parentalConsentTab)
            }
        })
    }

    private fun setupClickListeners() {
        addClickListenerToConsentAccept()
        addClickListenerToConsentDecline()
        addClickListenerToPrivacyNotice()
    }

    private fun addClickListenerToConsentAccept() {
        consentAcceptButton.setOnClickListener {
            consentEvents.postValue(buildConsentEventForResult(ACCEPTED))
        }
    }

    private fun addClickListenerToConsentDecline() {
        consentDeclineButton.setOnClickListener {
            consentEvents.postValue(buildConsentEventForResult(DECLINED))
        }
    }

    private fun buildConsentEventForResult(consentResult: ConsentEvent.Result) =
        ConsentEvent(startConsentEventTime, timeHelper.now(), getCurrentConsentTab(), consentResult)

    private fun getCurrentConsentTab() = when(tabHost.currentTabTag) {
        GENERAL_CONSENT_TAB_TAG -> INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> PARENTAL
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
