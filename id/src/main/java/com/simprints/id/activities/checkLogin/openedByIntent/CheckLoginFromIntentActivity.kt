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
import com.simprints.id.tools.extensions.isCallingAppFromUnknownSource
import com.simprints.id.tools.extensions.launchAlert
import org.jetbrains.anko.startActivityForResult

// App launched when user open SimprintsID using a client app (by intent)
open class CheckLoginFromIntentActivity : AppCompatActivity(), CheckLoginFromIntentContract.View {

    companion object {
        const val LOGIN_REQUEST_CODE: Int = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 1
        private const val LAUNCH_ACTIVITY_REQUEST_CODE = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 2
        private const val ALERT_ACTIVITY_REQUEST_CODE = InternalConstants.LAST_GLOBAL_REQUEST_CODE + 3
    }

    override lateinit var viewPresenter: CheckLoginFromIntentContract.Presenter

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

    override fun parseCallout(): Callout =
        intent.toCallout()

    override fun checkCallingAppIsFromKnownSource() {
        dataManager.callingPackage = getCallingPackageName()
        if (app.packageManager.isCallingAppFromUnknownSource(dataManager.callingPackage)) {
            dataManager.logSafeException(CallingAppFromUnknownSourceException())
        }
    }

    fun getCallingPackageName(): String {
        return callingPackage ?: ""
    }

    override fun openAlertActivityForError(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun openLoginActivity() {
        startActivityForResult<LoginActivity>(LOGIN_REQUEST_CODE)
    }

    override fun finishCheckLoginFromIntentActivity() {
        finish()
    }

    override fun openLaunchActivity() {
        val nextIntent = Intent(this, LaunchActivity::class.java)
        startActivityForResult(nextIntent, LAUNCH_ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // We need to call setResult and finish when the either MainActivity returns a result
        // that needs to be forward back to the calling app or the user tapped on "close" button (RESULT_CANCELED)
        // in a error screen.
        // If the activity doesn't finish, then we check again the SignedInState in onResume.
        if (requestCode == LAUNCH_ACTIVITY_REQUEST_CODE ||
            requestCode == ALERT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {

            setResult(resultCode, data)
            finish()
        }
    }
}
