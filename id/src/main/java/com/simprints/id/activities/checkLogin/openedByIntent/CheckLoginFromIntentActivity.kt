package com.simprints.id.activities.checkLogin.openedByIntent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.Alert
import com.simprints.id.domain.requests.Request
import com.simprints.id.exceptions.unexpected.CallingAppFromUnknownSourceException
import com.simprints.id.moduleapi.DomainToFingerprintAdapter.fromDomainToFingerprintRequest
import com.simprints.id.moduleapi.FingerprintToDomainAdapter.fromFingerprintToDomainResponse
import com.simprints.id.moduleapi.fromDomainToClientApiAdapter.fromDomainToClientApiResponse
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.ALERT_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.LAUNCH_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.LOGIN_ACTIVITY_REQUEST
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.*
import com.simprints.moduleapi.app.responses.IAppResponse
import com.simprints.moduleapi.fingerprint.requests.IFingerprintRequest
import com.simprints.moduleapi.fingerprint.responses.IFingerprintResponse
import javax.inject.Inject

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var timeHelper: TimeHelper

    override lateinit var viewPresenter: CheckLoginFromIntentContract.Presenter

    private val app: Application by lazy { application as Application }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = CheckLoginFromIntentPresenter(this, component)

        viewPresenter.setup()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun getAppVersionNameFromPackageManager() = packageVersionName
    override fun getDeviceUniqueId() = deviceId

    override fun parseRequest() =
        intent.parseClientApiRequest() as Request

    override fun getCheckCallingApp() = getCallingPackageName()

    override fun checkCallingAppIsFromKnownSource() {
        if (app.packageManager.isCallingAppFromUnknownSource(callingPackage)) {
            crashReportManager.logExceptionOrThrowable(CallingAppFromUnknownSourceException())
        }
    }

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alert: Alert) {
        launchAlert(alert)
    }

    override fun openLoginActivity(appRequest: Request) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.putExtra(Request.BUNDLE_KEY, appRequest)
        startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST)
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }

    override fun openLaunchActivity(appRequest: Request) {
        val fingerprintsModule = "com.simprints.id" //STOPSHIP
        val launchActivityClassName = "com.simprints.fingerprint.activities.launch.LaunchActivity"

        val intent = Intent().setClassName(fingerprintsModule, launchActivityClassName)
            .also { it.putExtra(IFingerprintRequest.BUNDLE_KEY, fromDomainToFingerprintRequest(appRequest, preferencesManager)) }
        startActivityForResult(intent, LAUNCH_ACTIVITY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // We need to call setResult and finish when the either CollectFingerprintsActivity returns a result
        // that needs to be forward back to the calling app or the user tapped on "close" button (RESULT_CANCELED)
        // in a error screen.
        // If the activity doesn't finish, then we check again the SignedInState in onResume.
        if (requestCode == LAUNCH_ACTIVITY_REQUEST ||
            requestCode == ALERT_ACTIVITY_REQUEST && resultCode == Activity.RESULT_OK) {

            val fingerprintResponse = data?.extras?.getParcelable<IFingerprintResponse>(IFingerprintResponse.BUNDLE_KEY)
            fingerprintResponse?.let {
                val domainResponse = fromFingerprintToDomainResponse(fingerprintResponse)
                viewPresenter.handleActivityResult(requestCode, resultCode, domainResponse)

                val clientApiResponse = fromDomainToClientApiResponse(domainResponse)
                setResult(resultCode, Intent().apply { putExtra(IAppResponse.BUNDLE_KEY, clientApiResponse) })
            }
        }

        finish()
    }
}
