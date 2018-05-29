package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.view.MenuItem
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.dashboard.views.WrapContentLinearLayoutManager
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.utils.AndroidResourcesHelperImpl
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.app_bar_dashboard.*
import kotlinx.android.synthetic.main.content_dashboard.*
import org.jetbrains.anko.support.v4.onRefresh

class DashboardActivity : AppCompatActivity(), DashboardContract.View, NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 2
        private const val ABOUT_ACTIVITY_REQUEST_CODE = 3
    }

    override lateinit var viewPresenter: DashboardContract.Presenter
    private val app: Application by lazy { application as Application }

    private lateinit var cardsViewAdapter: DashboardCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        LanguageHelper.setLanguage(this, app.dataManager.preferences.language)

        val syncClient = SyncService.getClient(this)
        viewPresenter = DashboardPresenter(this, syncClient,
            app.dataManager, app.dbManager, app.loginInfoManager, app.preferencesManager,
            AndroidResourcesHelperImpl(app))

        initDrawer()
        initCards()
    }

    private fun initDrawer() {
        setSupportActionBar(dashboardToolbar)
        val toggle = ActionBarDrawerToggle(
            this, dashboardDrawerLayout, dashboardToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        dashboardDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        dashboardNavigationView.setNavigationItemSelectedListener(this)
    }

    private fun initCards() {
        initRecyclerCardViews(viewPresenter)
        initSwipeRefreshLayout(viewPresenter)
    }

    private fun initRecyclerCardViews(viewPresenter: DashboardContract.Presenter) {
        cardsViewAdapter = DashboardCardAdapter(viewPresenter.cardsModelsList)
        dashboardCardsView.also {
            it.setHasFixedSize(false)
            it.itemAnimator = DefaultItemAnimator()
            it.layoutManager = WrapContentLinearLayoutManager(this)
            it.adapter = cardsViewAdapter
        }
    }

    private fun initSwipeRefreshLayout(viewPresenter: DashboardContract.Presenter) {
        swipeRefreshLayout.onRefresh {
            viewPresenter.userDidWantToRefreshCardsIfPossible()
        }
    }

    override fun notifyCardViewChanged(position: Int) {
        runOnUiThread {
            cardsViewAdapter.notifyItemChanged(position)
        }
    }

    override fun updateCardViews() {
        runOnUiThread {
            cardsViewAdapter.notifyDataSetChanged()
        }
    }

    override fun stopRequestIfRequired() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_help -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            R.id.privacy -> startActivityForResult(Intent(this, PrivacyActivity::class.java), PRIVACY_ACTIVITY_REQUEST_CODE)
            R.id.nav_about -> startActivityForResult(Intent(this, AboutActivity::class.java), ABOUT_ACTIVITY_REQUEST_CODE)
            R.id.nav_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
            R.id.logout -> logout()
        }
        dashboardDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        app.loginInfoManager.cleanCredentials()
        app.dataManager.db.signOut()
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

    override fun getStringWithParams(stringRes: Int, currentValue: Int, maxValue: Int): String {
        return getString(stringRes, currentValue, maxValue)
    }

    override fun launchAlertView(error: ALERT_TYPE) {
        this.launchAlert(error)
    }
}
