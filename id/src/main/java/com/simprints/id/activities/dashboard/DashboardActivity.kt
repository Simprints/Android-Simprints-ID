package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.tools.AndroidResourcesHelper
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard_card_project_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardActivity : AppCompatActivity() {

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var projectDetailsCardDisplayer: DashboardProjectDetailsCardDisplayer
    @Inject lateinit var syncCardDisplayer: DashboardSyncCardDisplayer
    @Inject lateinit var viewModelFactory: DashboardViewModelFactory

    private lateinit var viewModel: DashboardViewModel

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val component = (application as Application).component
        component.inject(this)
        title = androidResourcesHelper.getString(R.string.dashboard_label)

        setupActionBar()
        setupViewModel()
        setupCards()

        observeForProjectDetails()
        observeForSyncCardState()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse =
            AlertActivityHelper.extractPotentialAlertScreenResponse(data)

        if (potentialAlertScreenResponse != null) {
            finish()
        }

        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            startCheckLoginActivityAndFinish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)

        menu?.findItem(R.id.menuSettings)?.title =
            androidResourcesHelper.getString(R.string.menu_settings)
        menu?.findItem(R.id.menuPrivacyNotice)?.title =
            androidResourcesHelper.getString(R.string.menu_privacy_notice)

        return true
    }

    private fun setupActionBar() {
        dashboardToolbar.title = androidResourcesHelper.getString(R.string.dashboard_label)
        setSupportActionBar(dashboardToolbar)
        supportActionBar?.elevation = 4F

        setMenuItemClickListener()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(
            DashboardViewModel::class.java
        )
    }

    private fun setupCards() {
        projectDetailsCardDisplayer.initRoot(dashboard_project_details_card)
        syncCardDisplayer.initRoot(dashboard_sync_card)
    }

    private fun observeForProjectDetails() {
        CoroutineScope(Dispatchers.Main).launch {
            val projectDetails = viewModel.getProjectDetails()
            projectDetails.observe(this@DashboardActivity, Observer {
                projectDetailsCardDisplayer.displayProjectDetails(it)
            })
        }
    }

    private fun observeForSyncCardState() {
        viewModel.syncCardState.observe(this, Observer<DashboardSyncCardState> {
            syncCardDisplayer.displayState(it)
        })
        viewModel.emit()
    }

    private fun setMenuItemClickListener() {
        dashboardToolbar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.menuPrivacyNotice -> startActivity(
                    Intent(
                        this,
                        PrivacyNoticeActivity::class.java
                    )
                )
                R.id.menuSettings -> startActivityForResult(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    ), SETTINGS_ACTIVITY_REQUEST_CODE
                )
                R.id.debug -> startActivity(Intent(this, DebugActivity::class.java))
            }
            true
        }
    }

    private fun startCheckLoginActivityAndFinish() {
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

}
