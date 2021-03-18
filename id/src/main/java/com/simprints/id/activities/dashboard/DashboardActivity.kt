package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.tools.activity.BaseSplitActivity
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState.SyncConnecting
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.services.sync.events.master.EventSyncManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard_card_daily_activity.*
import kotlinx.android.synthetic.main.activity_dashboard_card_project_details.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DashboardActivity : BaseSplitActivity() {

    private var syncAgainTicker: ReceiveChannel<Unit>? = null

    @Inject lateinit var projectDetailsCardDisplayer: DashboardProjectDetailsCardDisplayer
    @Inject lateinit var syncCardDisplayer: DashboardSyncCardDisplayer
    @Inject lateinit var dailyActivityCardDisplayer: DashboardDailyActivityCardDisplayer
    @Inject lateinit var viewModelFactory: DashboardViewModelFactory
    @Inject lateinit var eventSyncManager: EventSyncManager
    @Inject lateinit var settingsPreferencesManager: SettingsPreferencesManager

    private lateinit var viewModel: DashboardViewModel

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
        private const val ONE_MINUTE = 1000 * 60L
        private const val TIME_FOR_CHECK_IF_SYNC_REQUIRED = 1 * ONE_MINUTE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = (application as Application).component
        component.inject(this)
        setContentView(R.layout.activity_dashboard)
        title = getString(R.string.dashboard_label)

        setupActionBar()
        setupViewModel()
        setupCards()
        observeCardData()
        loadDailyActivity()
    }

    private fun setupActionBar() {
        dashboardToolbar.title = getString(R.string.dashboard_label)
        setSupportActionBar(dashboardToolbar)
        supportActionBar?.elevation = 4F

        setMenuItemClickListener()
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
                R.id.debug -> if (BuildConfig.DEBUG_MODE) {
                    startActivity(Intent(this, DebugActivity::class.java))
                }
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)

        menu?.run {
            this.findItem(R.id.debug)?.isVisible = BuildConfig.DEBUG_MODE

            findItem(R.id.menuSettings).title =
                getString(R.string.menu_settings)

            with(findItem(R.id.menuPrivacyNotice)) {
                title = getString(R.string.menu_privacy_notice)
                isVisible = settingsPreferencesManager.consentRequired
            }
        }

        return true
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(
            DashboardViewModel::class.java
        )
    }

    private fun setupCards() {
        projectDetailsCardDisplayer.initRoot(dashboard_project_details_card)
        syncCardDisplayer.initRoot(dashboard_sync_card)
        dailyActivityCardDisplayer.initRoot(dashboard_daily_activity_card_root)
    }

    private fun observeCardData() {
        observeForProjectDetails()
        observeForSyncCardState()
    }

    private fun observeForProjectDetails() {
        viewModel.getProjectDetails().observe(this, Observer {
            projectDetailsCardDisplayer.displayProjectDetails(it)
        })
    }

    private fun observeForSyncCardState() {
        viewModel.syncCardStateLiveData.observe(this, Observer {
            syncCardDisplayer.displayState(it)
        })

        syncCardDisplayer.userWantsToOpenSettings.observe(this, LiveDataEventObserver {
            openSettings()
        })

        syncCardDisplayer.userWantsToSelectAModule.observe(this, LiveDataEventObserver {
            openSelectModules()
        })

        syncCardDisplayer.userWantsToSync.observe(this, LiveDataEventObserver {
            syncCardDisplayer.displayState(SyncConnecting(null, 0, null))
            eventSyncManager.sync()
        })
    }

    private fun loadDailyActivity() {
        viewModel.getDailyActivity().let {
            if (it.hasNoActivity()) {
                dashboard_daily_activity_card.visibility = View.GONE
            } else {
                dashboard_daily_activity_card.visibility = View.VISIBLE
                dailyActivityCardDisplayer.displayDailyActivityState(it)
            }
        }
    }

    private fun openSettings() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun openSelectModules() {
        startActivity(Intent(this, ModuleSelectionActivity::class.java))
    }

    @ObsoleteCoroutinesApi
    override fun onResume() {
        super.onResume()
        loadDailyActivity()

        lifecycleScope.launch {
            stopTickerToCheckIfSyncIsRequired()
            syncAgainTicker = ticker(
                delayMillis = TIME_FOR_CHECK_IF_SYNC_REQUIRED,
                initialDelayMillis = 0
            ).also {
                for (event in it) {
                    Timber.tag(SYNC_LOG_TAG).d("[ACTIVITY] Launch sync if required")
                    viewModel.syncIfRequired()
                }
            }

            lifecycleScope.launch {
                syncCardDisplayer.startTickerToUpdateLastSyncText()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopTickerToCheckIfSyncIsRequired()
        syncCardDisplayer.stopOngoingTickerToUpdateLastSyncText()
    }

    private fun stopTickerToCheckIfSyncIsRequired() {
        syncAgainTicker?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse =
            AlertActivityHelper.extractPotentialAlertScreenResponse(data)

        if (potentialAlertScreenResponse != null) {
            finish()
        }

        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            startRequestLoginActivityAndFinish()
        }
    }

    private fun startRequestLoginActivityAndFinish() {
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

}
