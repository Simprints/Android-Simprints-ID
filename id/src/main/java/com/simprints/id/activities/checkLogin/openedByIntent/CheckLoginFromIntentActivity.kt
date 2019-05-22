package com.simprints.id.activities.checkLogin.openedByIntent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.login.request.LoginActivityRequest
import com.simprints.id.activities.orchestrator.OrchestratorActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.alert.NewAlert
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.tools.InternalConstants.RequestIntents.Companion.LOGIN_ACTIVITY_REQUEST
import com.simprints.id.tools.extensions.*
import javax.inject.Inject

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override lateinit var viewPresenter: CheckLoginFromIntentContract.Presenter

    private val app: Application by lazy { application as Application }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = CheckLoginFromIntentPresenter(this, deviceId, component)

        viewPresenter.setup()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun parseRequest() =
        intent.parseAppRequest() as AppRequest

    override fun getCheckCallingApp() = getCallingPackageName()

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alert: NewAlert) {
        launchAlert(alert)
    }

    override fun openLoginActivity(appRequest: AppRequest) {
        val loginIntent = Intent(this, LoginActivity::class.java)
        loginIntent.putExtra(LoginActivityRequest.BUNDLE_KEY, LoginActivityRequest(appRequest.projectId, appRequest.userId))
        startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST)
    }

    override fun openOrchestratorActivity(appRequest: AppRequest) {
        val intent = Intent(this, OrchestratorActivity::class.java).apply {
            putExtra(AppRequest.BUNDLE_KEY, appRequest)
            addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        }
        startActivity(intent)
        finish()
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }
}
