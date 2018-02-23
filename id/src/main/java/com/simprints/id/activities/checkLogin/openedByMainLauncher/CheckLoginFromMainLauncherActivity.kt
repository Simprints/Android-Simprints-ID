package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.extensions.launchAlert
import org.jetbrains.anko.startActivity

// App launched when user open SimprintsID using the Home button
open class CheckLoginFromMainLauncherActivity : AppCompatActivity(), CheckLoginFromMainLauncherContract.View {

    private lateinit var viewPresenter: CheckLoginFromMainLauncherContract.Presenter
    private val app: Application by lazy { application as Application }
    private val dataManager: DataManager by lazy { app.dataManager }
    private val timeHelper by lazy { app.timeHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        viewPresenter = CheckLoginFromMainLauncherPresenter(
            this,
            dataManager,
            timeHelper)
    }

    override fun setPresenter(presenter: CheckLoginFromMainLauncherContract.Presenter) {
        viewPresenter = presenter
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun launchAlertForError(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun openRequestLoginActivity() {
        startActivity<RequestLoginActivity>()
        finish()
    }

    override fun openDashboardActivity() {
        val dashIntent = Intent(this, DashboardActivity::class.java)
        startActivity(dashIntent)
        finish()
    }
}
