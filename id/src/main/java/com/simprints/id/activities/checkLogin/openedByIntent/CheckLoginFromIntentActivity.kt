package com.simprints.id.activities.checkLogin.openedByIntent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.requests.IdRequest
import com.simprints.id.exceptions.unexpected.CallingAppFromUnknownSourceException
import com.simprints.id.session.callout.Callout.Companion.toCallout
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.isCallingAppFromUnknownSource
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.parseClientApiRequest
import javax.inject.Inject

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var timeHelper: TimeHelper

    companion object {
        const val LOGIN_REQUEST_CODE: Int = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 1
        private const val LAUNCH_ACTIVITY_REQUEST_CODE = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 2
        private const val ALERT_ACTIVITY_REQUEST_CODE = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 3
    }

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

    override fun parseIdRequest() =
        intent.parseClientApiRequest() as IdRequest

    override fun getCheckCallingApp() = getCallingPackageName()

    override fun checkCallingAppIsFromKnownSource() {
        preferencesManager.callingPackage = getCallingPackageName()
        if (app.packageManager.isCallingAppFromUnknownSource(preferencesManager.callingPackage)) {
            crashReportManager.logExceptionOrThrowable(CallingAppFromUnknownSourceException())
        }
    }

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun openLoginActivity(legacyApiKey: String) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.putExtra(IntentKeys.loginActivityLegacyProjectIdKey, legacyApiKey)
        startActivityForResult(loginIntent, LOGIN_REQUEST_CODE)
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }

    override fun openLaunchActivity() {
        val nextIntent = Intent(this, LaunchActivity::class.java)
        startActivityForResult(nextIntent, LAUNCH_ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // We need to call setResult and finish when the either CollectFingerprintsActivity returns a result
        // that needs to be forward back to the calling app or the user tapped on "close" button (RESULT_CANCELED)
        // in a error screen.
        // If the activity doesn't finish, then we check again the SignedInState in onResume.
        if (requestCode == LAUNCH_ACTIVITY_REQUEST_CODE ||
            requestCode == ALERT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {

            viewPresenter.handleActivityResult(requestCode, resultCode, data.toCallout())
            setResult(resultCode, data)
            finish()
        }
    }
}
