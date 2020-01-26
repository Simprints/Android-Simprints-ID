package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.alert.AlertActivityHelper
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardState
import com.simprints.id.activities.debug.DebugActivity
import com.simprints.id.activities.longConsent.PrivacyNoticeActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.ModuleSelectionActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class DashboardActivity : AppCompatActivity() {

    private var syncAgainTicker: ReceiveChannel<Unit>? = null

    @Inject lateinit var androidResourcesHelper: AndroidResourcesHelper
    @Inject lateinit var syncCardDisplayer: DashboardSyncCardDisplayer
    @Inject lateinit var peopleSyncManager: PeopleSyncManager
    @Inject lateinit var deviceManager: DeviceManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository
    @Inject lateinit var peopleSyncCache: PeopleSyncCache
    @Inject lateinit var timeHelper: TimeHelper

    private lateinit var viewModel: DashboardViewModel
    private lateinit var viewModelFactory: DashboardViewModelFactory

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LOGOUT_RESULT_CODE = 1
        private const val TIME_FOR_CHECK_IF_SYNC_REQUIRED = 1000 * 30L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val component = (application as Application).component
        component.inject(this)
        title = androidResourcesHelper.getString(R.string.dashboard_label)
        viewModelFactory = DashboardViewModelFactory(peopleSyncManager, deviceManager, preferencesManager, peopleDownSyncScopeRepository, peopleSyncCache, timeHelper)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)
        setupActionBar()

        observeForSyncCardState()
    }

    private fun observeForSyncCardState() {
        syncCardDisplayer.initRoot(dashboard_sync_card)
        viewModel.syncCardStateLiveData.observe(this@DashboardActivity, Observer<DashboardSyncCardState> {
            syncCardDisplayer.displayState(it)
        })

        syncCardDisplayer.userWantsToOpenSettings.observe(this@DashboardActivity, Observer {
            openSettings()
        })

        syncCardDisplayer.userWantsToSelectAModule.observe(this@DashboardActivity, Observer {
            openSelectModules()
        })

        syncCardDisplayer.userWantsToSync.observe(this@DashboardActivity, Observer {
            peopleSyncManager.sync()
        })

        lifecycleScope.launchWhenResumed {
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            stopTickerToCheckIfSyncIsRequired()
            syncAgainTicker = ticker(delayMillis = TIME_FOR_CHECK_IF_SYNC_REQUIRED, initialDelayMillis = 100)
            syncAgainTicker?.let {
                for (event in it) {
                    Timber.d("Launch sync if required")
                    viewModel.syncIfRequired()
                }
            }
        }

        lifecycleScope.launch {
            syncCardDisplayer.startTickerToUpdateLastSyncText()
        }
    }

    override fun onPause() {
        super.onPause()
        stopTickerToCheckIfSyncIsRequired()
        syncCardDisplayer.stopTickerToUpdateLastSyncText()
    }

    private fun stopTickerToCheckIfSyncIsRequired() {
        syncAgainTicker?.cancel()
    }

    private fun openSettings() {
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun openSelectModules() {
        startActivity(Intent(this, ModuleSelectionActivity::class.java))
    }

    private fun setupActionBar() {
        dashboardToolbar.title = androidResourcesHelper.getString(R.string.dashboard_label)
        setSupportActionBar(dashboardToolbar)
        supportActionBar?.elevation = 4F

        setMenuItemClickListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)

        menu?.findItem(R.id.menuSettings)?.title = androidResourcesHelper.getString(R.string.menu_settings)
        menu?.findItem(R.id.menuPrivacyNotice)?.title = androidResourcesHelper.getString(R.string.menu_privacy_notice)

        return true
    }

    private fun setMenuItemClickListener() {
        dashboardToolbar.setOnMenuItemClickListener { menuItem ->

            when (menuItem.itemId) {
                R.id.menuPrivacyNotice -> startActivity(Intent(this, PrivacyNoticeActivity::class.java))
                R.id.menuSettings -> startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
                R.id.debug -> startActivity(Intent(this, DebugActivity::class.java))
            }
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val potentialAlertScreenResponse = AlertActivityHelper.extractPotentialAlertScreenResponse(data)
        if (potentialAlertScreenResponse != null) {
            finish()
        }

        if (resultCode == LOGOUT_RESULT_CODE && requestCode == SETTINGS_ACTIVITY_REQUEST_CODE) {
            startCheckLoginActivityAndFinish()
        }
    }

    private fun startCheckLoginActivityAndFinish() {
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }
}
