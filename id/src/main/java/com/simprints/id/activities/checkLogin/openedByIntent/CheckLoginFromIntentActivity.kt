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
        // We need to call setResult and finish when the either MainActivity returns a result
        // that needs to be forward back to the calling app or the user tapped on "close" button (RESULT_CANCELED)
        // in a error screen.
        // If the activity doesn't finish, then we check again the SignedInState in onResume.
        if (requestCode == MAIN_ACTIVITY_REQUEST ||
            requestCode == InternalConstants.ALERT_ACTIVITY_REQUEST && resultCode == Activity.RESULT_CANCELED) {

            setResult(resultCode, data)
            finish()
        }
    }
}
