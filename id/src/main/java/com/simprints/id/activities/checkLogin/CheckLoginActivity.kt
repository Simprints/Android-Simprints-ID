package com.simprints.id.activities.checkLogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.Callout.Companion.toCallout
import com.simprints.id.exceptions.safe.CallingAppFromUnknownSourceException
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST
import com.simprints.id.tools.extensions.isCallingAppFromUnknownSource
import com.simprints.id.tools.extensions.launchAlert

open class CheckLoginActivity : AppCompatActivity(), CheckLoginContract.View {

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST: Int = 1
    }

    lateinit var viewPresenter: CheckLoginContract.Presenter
    private lateinit var app: Application
    private lateinit var dataManager: DataManager
    private val timeHelper by lazy { app.timeHelper }
    private val wasAppOpenedByIntent: Boolean by lazy {
        callingActivity != null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        app = application as Application
        dataManager = app.dataManager
        dataManager.callingPackage = getCallingPackageName()

        viewPresenter = CheckLoginPresenter(this, dataManager, app.sessionParametersExtractor, wasAppOpenedByIntent, timeHelper)
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun setPresenter(presenter: CheckLoginContract.Presenter) {
        viewPresenter = presenter
    }

    override fun parseCallout(): Callout =
        intent.toCallout()

    override fun checkCallingApp() {
        if (app.packageManager.isCallingAppFromUnknownSource(dataManager.callingPackage)) {
            dataManager.logSafeException(CallingAppFromUnknownSourceException())
        }
    }

    open fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun launchAlertForError(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }


    override fun openLoginActivity() {
        val nextIntent = Intent(this, LoginActivity::class.java)
        startActivityForResult(nextIntent, LOGIN_ACTIVITY_REQUEST)
    }

    override fun openRequestLoginActivity() {
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }


    override fun startActivity(nextActivityClassAfterLogin: Class<out Any>) {
        try {
            val nextIntent = Intent(this, nextActivityClassAfterLogin)
            startActivityForResult(nextIntent, MAIN_ACTIVITY_REQUEST)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOGIN_ACTIVITY_REQUEST) {
            viewPresenter.checkIfUserIsLoggedIn()
        } else {
            setResult(resultCode, data)
            finish()
        }
    }
}
