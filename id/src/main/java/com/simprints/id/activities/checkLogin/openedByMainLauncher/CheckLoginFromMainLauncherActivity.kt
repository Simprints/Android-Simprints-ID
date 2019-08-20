package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.domain.alert.AlertType
import org.jetbrains.anko.startActivity

// App launched when user open SimprintsID using the Home button
open class CheckLoginFromMainLauncherActivity : AppCompatActivity(), CheckLoginFromMainLauncherContract.View {

    override lateinit var viewPresenter: CheckLoginFromMainLauncherContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = CheckLoginFromMainLauncherPresenter(this, component)
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun openAlertActivityForError(alertType: AlertType) {
        launchAlert(this, alertType)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse = AlertActivityHelper.extractPotentialAlertScreenResponse(data)
        if (potentialAlertScreenResponse != null) {
            finish()
        }
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
