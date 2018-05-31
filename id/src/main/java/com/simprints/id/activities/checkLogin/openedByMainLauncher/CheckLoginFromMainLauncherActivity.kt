package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.launchAlert
import org.jetbrains.anko.startActivity
import javax.inject.Inject

// App launched when user open SimprintsID using the Home button
open class CheckLoginFromMainLauncherActivity : AppCompatActivity(), CheckLoginFromMainLauncherContract.View {

    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var secureDataManager: SecureDataManager

    override lateinit var viewPresenter: CheckLoginFromMainLauncherContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)
        (application as Application).component.inject(this)

        viewPresenter = CheckLoginFromMainLauncherPresenter(
            this,
            dataManager,
            secureDataManager,
            timeHelper)
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun openAlertActivityForError(alertType: ALERT_TYPE) {
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
