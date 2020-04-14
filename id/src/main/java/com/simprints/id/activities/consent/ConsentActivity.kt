package com.simprints.id.activities.consent

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.Intent
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.TabHost
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.db.session.domain.models.events.ConsentEvent
import com.simprints.id.data.db.session.domain.models.session.Location
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.moduleapi.core.requests.AskConsentRequest
import com.simprints.id.domain.moduleapi.core.response.AskConsentResponse
import com.simprints.id.domain.moduleapi.core.response.ConsentResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse
import com.simprints.id.domain.moduleapi.core.response.CoreResponse.Companion.CORE_STEP_BUNDLE
import com.simprints.id.exceptions.safe.FailedToRetrieveUserLocation
import com.simprints.id.exceptions.unexpected.InvalidAppRequest
import com.simprints.id.exitformhandler.ExitFormHelper
import com.simprints.id.orchestrator.steps.core.CoreRequestCode
import com.simprints.id.orchestrator.steps.core.CoreResponseCode
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.LocationManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.getNotGrantedPermissions
import com.simprints.id.tools.extensions.requestPermissionsIfRequired
import kotlinx.android.synthetic.main.activity_consent.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConsentActivity : AppCompatActivity() {

    private lateinit var viewModel: ConsentViewModel
    private lateinit var generalConsentTab: TabHost.TabSpec
    private lateinit var parentalConsentTab: TabHost.TabSpec
    private lateinit var askConsentRequestReceived: AskConsentRequest

    @Inject lateinit var viewModelFactory: ConsentViewModelFactory
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var exitFormHelper: ExitFormHelper
    @Inject lateinit var sessionRepository: SessionRepository
    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var locationManager: LocationManager
    @Inject lateinit var crashReportManager: CrashReportManager

    private var startConsentEventTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)

        injectDependencies()

        startConsentEventTime = timeHelper.now()

        askConsentRequestReceived = intent.extras?.getParcelable(CORE_STEP_BUNDLE) ?: throw InvalidAppRequest()

        viewModel = ViewModelProvider(this, viewModelFactory.apply { askConsentRequest = askConsentRequestReceived })
            .get(ConsentViewModel::class.java)

        showLogoIfNecessary()
        requestLocationPermission()
        setupTextInUi()
        setupTabs()
        setupObserversForUi()
    }

    private fun injectDependencies() {
        val component = (application as Application).component
        component.inject(this)
    }

    private fun showLogoIfNecessary() {
        if (preferencesManager.logoExists) {
            simprintsLogoWithTagLine.visibility = View.VISIBLE
        } else {
            simprintsLogoWithTagLine.visibility = View.GONE
        }
    }

    private fun requestLocationPermission() {
        val requestingPermissions = requestPermissionsIfRequired(arrayOf(ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)

        if (!requestingPermissions) {
            storeUserLocationIntoCurrentSession()
        }
    }

    private fun setupTextInUi() {
        with(androidResourcesHelper) {
            consentDeclineButton.text = getString(R.string.launch_consent_decline_button)
            consentAcceptButton.text = getString(R.string.launch_consent_accept_button)
            privacyNoticeText.text = getString(R.string.privacy_notice_text)
            privacyNoticeText.paintFlags = privacyNoticeText.paintFlags or UNDERLINE_TEXT_FLAG
        }
    }

    private fun setupTabs() {
        tabHost.setup()

        generalConsentTab = tabHost.newTabSpec(GENERAL_CONSENT_TAB_TAG)
            .setIndicator(androidResourcesHelper.getString(R.string.consent_general_title))
            .setContent(R.id.generalConsentTextView)

        parentalConsentTab = tabHost.newTabSpec(PARENTAL_CONSENT_TAB_TAG)
            .setIndicator(androidResourcesHelper.getString(R.string.consent_parental_title))
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

    fun handleConsentAcceptClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(ConsentEvent.Result.ACCEPTED))
        setResult(CoreResponseCode.CONSENT.value, Intent().apply {
            putExtra(CORE_STEP_BUNDLE, AskConsentResponse(ConsentResponse.ACCEPTED))
        })
        finish()
    }

    fun handleConsentDeclineClick(@Suppress("UNUSED_PARAMETER") view: View) {
        viewModel.addConsentEvent(buildConsentEventForResult(ConsentEvent.Result.DECLINED))
        startExitFormActivity()
    }

    fun handlePrivacyNoticeClick(@Suppress("UNUSED_PARAMETER") view: View) {
        startPrivacyNoticeActivity()
    }

    private fun buildConsentEventForResult(consentResult: ConsentEvent.Result) =
        ConsentEvent(startConsentEventTime, timeHelper.now(), getCurrentConsentTab(), consentResult)

    private fun getCurrentConsentTab() = when (tabHost.currentTabTag) {
        GENERAL_CONSENT_TAB_TAG -> ConsentEvent.Type.INDIVIDUAL
        PARENTAL_CONSENT_TAB_TAG -> ConsentEvent.Type.PARENTAL
        else -> throw IllegalStateException("Invalid consent tab selected")
    }

    private fun startPrivacyNoticeActivity() {
        startActivity(Intent(this, PrivacyNoticeActivity::class.java))
    }

    private fun startExitFormActivity() {
        val exitFormActivityClass =
            exitFormHelper.getExitFormActivityClassFromModalities(preferencesManager.modalities)

        exitFormActivityClass?.let {
            startActivityForResult(
                Intent().setClassName(this, it),
                CoreRequestCode.EXIT_FORM.value
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        exitFormHelper.buildExitFormResponseForCore(data)?.let {
            setResultAndFinish(it)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && getNotGrantedPermissions(arrayOf(ACCESS_FINE_LOCATION)).isEmpty()) {
            storeUserLocationIntoCurrentSession()
        }
    }

    private fun storeUserLocationIntoCurrentSession() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val locationRequest = LocationRequest().apply {
                    priority = PRIORITY_HIGH_ACCURACY
                }
                val locationsFlow = locationManager.requestLocation(locationRequest).take(1)
                locationsFlow.collect { locations ->
                    val lastLocation = locations.last()
                    sessionRepository.updateCurrentSession {
                        Timber.d("Saving user's location into the current session")
                        it.location = Location(lastLocation.latitude, lastLocation.longitude)
                    }
                }
            } catch (t: Throwable) {
                crashReportManager.logExceptionOrSafeException(FailedToRetrieveUserLocation(t))
            }
        }
    }

    private fun setResultAndFinish(coreResponse: CoreResponse) {
        setResult(Activity.RESULT_OK, buildIntentForResponse(coreResponse))
        finish()
    }

    private fun buildIntentForResponse(coreResponse: CoreResponse) = Intent().apply {
        putExtra(CORE_STEP_BUNDLE, coreResponse)
    }

    override fun onBackPressed() {
        startExitFormActivity()
    }

    companion object {
        const val GENERAL_CONSENT_TAB_TAG = "General"
        const val PARENTAL_CONSENT_TAB_TAG = "Parental"
        const val LOCATION_PERMISSION_REQUEST_CODE = 99
    }
}
