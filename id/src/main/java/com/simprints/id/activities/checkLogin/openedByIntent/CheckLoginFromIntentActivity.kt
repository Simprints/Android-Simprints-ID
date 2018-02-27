package com.simprints.id.activities.checkLogin.openedByIntent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.Callout.Companion.toCallout
import com.simprints.id.exceptions.safe.CallingAppFromUnknownSourceException
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.InternalConstants
import com.simprints.id.tools.InternalConstants.MAIN_ACTIVITY_REQUEST
import com.simprints.id.tools.extensions.isCallingAppFromUnknownSource
import com.simprints.id.tools.extensions.launchAlert
import org.jetbrains.anko.startActivityForResult

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    private lateinit var viewPresenter: CheckLoginFromIntentContract.Presenter
    private val app: Application by lazy { application as Application }
    private val dataManager: DataManager by lazy { app.dataManager }
    private val timeHelper by lazy { app.timeHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        viewPresenter = CheckLoginFromIntentPresenter(
            this,
            dataManager,
            app.sessionParametersExtractor,
            timeHelper)

        viewPresenter.setup()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun setPresenter(presenter: CheckLoginFromIntentContract.Presenter) {
        viewPresenter = presenter
    }

    override fun parseCallout(): Callout =
        intent.toCallout()

    override fun checkCallingApp() {
        dataManager.callingPackage = getCallingPackageName()
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
        startActivityForResult<LoginActivity>(LoginActivity.LOGIN_REQUEST_CODE)
    }

    override fun finishAct() {
        finish()
    }

    override fun openLaunchActivity() {
        val nextIntent = Intent(this, LaunchActivity::class.java)
        startActivityForResult(nextIntent, MAIN_ACTIVITY_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //If it's a LOGIN_REQUEST_CODE or tryAgainAfter error, they will be handled in the onResume
        val isAlertErrorResultForCloseButton = requestCode == InternalConstants.ALERT_ACTIVITY_REQUEST && resultCode == Activity.RESULT_CANCELED
        if (requestCode != LoginActivity.LOGIN_REQUEST_CODE || isAlertErrorResultForCloseButton) {
            setResult(resultCode, data)
            finish()
        }
    }
}
