package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.extensions.launchAlert
import org.jetbrains.anko.startActivity

// App launched when user open SimprintsID using the Home button
open class CheckLoginFromMainLauncherActivity : AppCompatActivity(), CheckLoginFromMainLauncherContract.View {

    override lateinit var viewPresenter: CheckLoginFromMainLauncherContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_login)

        val component = (application as Application).component as AppComponent
        component.inject(this)

        viewPresenter = CheckLoginFromMainLauncherPresenter(this, component)
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
