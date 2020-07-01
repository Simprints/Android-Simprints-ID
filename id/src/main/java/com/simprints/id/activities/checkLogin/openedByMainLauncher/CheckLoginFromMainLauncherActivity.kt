package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.BaseSplitActivity
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.domain.alert.AlertType
import kotlinx.coroutines.launch
import org.jetbrains.anko.startActivity

// App launched when user open SimprintsID using the Home button
open class CheckLoginFromMainLauncherActivity : BaseSplitActivity(), CheckLoginFromMainLauncherContract.View {

    override lateinit var viewPresenter: CheckLoginFromMainLauncherContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Application).component.inject(this)
        setContentView(R.layout.splash_screen)
        title = getString(R.string.title_activity_front)

        val component = (application as Application).component
        component.inject(this)

        viewPresenter = CheckLoginFromMainLauncherPresenter(this, component)
    }


    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewPresenter.start()
        }
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
