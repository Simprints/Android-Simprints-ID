package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.dashboard.views.WrapContentLinearLayoutManager
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.services.sync.SyncService
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.utils.ResourcesHelperImpl
import org.jetbrains.anko.support.v4.onRefresh


class DashboardActivity : AppCompatActivity(), DashboardContract.View, NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 2
        private const val ABOUT_ACTIVITY_REQUEST_CODE = 3
    }

    override lateinit var viewPresenter: DashboardContract.Presenter
    private val app: Application by lazy { application as Application }
    private val dataManager: DataManager by lazy { app.dataManager }

    private lateinit var dashboardCards: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val syncClient = SyncService.getClient(this)
        viewPresenter = DashboardPresenter(this, syncClient, dataManager, ResourcesHelperImpl(app))

        initDrawer()
        initCards()
    }

    private lateinit var cardsViewAdapter: DashboardCardAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private fun initCards() {
        cardsViewAdapter = DashboardCardAdapter(viewPresenter.cardsModelsList)
        dashboardCards = (findViewById<RecyclerView>(R.id.dashboardCardsId)).also {
            it.setHasFixedSize(false)
            it.itemAnimator = DefaultItemAnimator()
            it.layoutManager = WrapContentLinearLayoutManager(this)
            it.adapter = cardsViewAdapter
        }

        swipeRefreshLayout = (findViewById<SwipeRefreshLayout>(R.id.dashboardCardsSwipeId)).apply {
            this.onRefresh {
                viewPresenter.didUserWantToRefreshCards()
            }
        }
    }

    override fun notifyCardViewChanged(position: Int) {
        cardsViewAdapter.notifyItemChanged(position)
    }

    override fun updateCardViews() {
        cardsViewAdapter.notifyDataSetChanged()
    }

    override fun stopRequestIfRequired() {
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    override fun onPause() {
        super.onPause()
        viewPresenter.pause()
    }

    private fun initDrawer() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        //syncItem = navigationView.menu.findItem(R.id.nav_sync)

        navigationView.setNavigationItemSelectedListener(this)
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        app.secureDataManager.cleanCredentials()
        app.dataManager.signOut()
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

    override fun showToast(messageRes: Int) {
        Toast.makeText(this,
            messageRes,
            Toast.LENGTH_LONG).show()
    }

    override fun getStringWithParams(stringRes: Int, currentValue: Int, maxValue: Int): String {
        return getString(stringRes, currentValue, maxValue)
    }

    override fun launchAlertView(error: ALERT_TYPE) {
        this.launchAlert(error)
    }
}
