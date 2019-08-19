package com.simprints.id.activities.checkLogin.openedByIntent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper.extractPotentialAlertScreenResponse
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.consent.ConsentActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.login.response.LoginActivityResponse
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.app.responses.AppErrorResponse
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.LOGIN_ACTIVITY_REQUEST
import com.simprints.id.tools.extensions.deviceId
import com.simprints.id.tools.extensions.parseAppRequest
import com.simprints.moduleapi.app.responses.IAppErrorResponse
import com.simprints.moduleapi.app.responses.IAppResponse
import javax.inject.Inject

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override lateinit var viewPresenter: CheckLoginFromIntentContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = CheckLoginFromIntentPresenter(this, deviceId, component)

        viewPresenter.setup()
        viewPresenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val potentialAlertScreenResponse = extractPotentialAlertScreenResponse(data)
        val appErrorResponseForLoginScreen = extractAppErrorResponseForLoginScreen(data)

        when {
            potentialAlertScreenResponse != null -> viewPresenter.onAlertScreenReturn(potentialAlertScreenResponse)
            appErrorResponseForLoginScreen != null -> viewPresenter.onLoginScreenErrorReturn(appErrorResponseForLoginScreen)
            else -> viewPresenter.checkSignedInStateIfPossible()
        }
    }

    private fun extractAppErrorResponseForLoginScreen(data: Intent?): AppErrorResponse? =
        data?.getParcelableExtra(LoginActivityResponse.BUNDLE_KEY)


    override fun setResultErrorAndFinish(appResponse: IAppErrorResponse) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(IAppResponse.BUNDLE_KEY, appResponse)
        })
        finish()
    }

    override fun parseRequest() =
        intent.parseAppRequest() as AppRequest

    override fun getCheckCallingApp() = getCallingPackageName()

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alertType: AlertType) {
        launchAlert(this, alertType)
    }

    override fun openLoginActivity(appRequest: AppRequest) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.putExtra(LoginActivityRequest.BUNDLE_KEY, LoginActivityRequest(appRequest.projectId, appRequest.userId))
        startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST)
    }

    override fun openOrchestratorActivity(appRequest: AppRequest) {
        val intent = Intent(this, ConsentActivity::class.java).apply {
            putExtra(AppRequest.BUNDLE_KEY, appRequest)
            //addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }
        startActivity(intent)
        finish()
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }
}
