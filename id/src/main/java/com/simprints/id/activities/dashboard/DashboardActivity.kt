package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.view.Menu
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.dashboard.views.WrapContentLinearLayoutManager
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.content_dashboard.*
import org.jetbrains.anko.support.v4.onRefresh
import javax.inject.Inject


class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var preferences: PreferencesManager
    @Inject
    lateinit var loginInfoManager: LoginInfoManager

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 2
    }

    override lateinit var viewPresenter: DashboardContract.Presenter
    private lateinit var cardsViewAdapter: DashboardCardAdapter

    private val notification = LongConsentNotification(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val component = (application as Application).component
        component.inject(this)
        setSupportActionBar(dashboardToolbar)
        LanguageHelper.setLanguage(this, preferences.language)

        viewPresenter = DashboardPresenter(this, component)
        setMenuItemClickListener()

        initCards()

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        return true
    }

    private fun setMenuItemClickListener() {
        dashboardToolbar.setOnMenuItemClickListener { menuItem ->

            val id = menuItem.itemId
            when (id) {
                R.id.menuPrivacyNotice -> startActivityForResult(Intent(this, PrivacyActivity::class.java), PRIVACY_ACTIVITY_REQUEST_CODE)
                R.id.menuSettings -> startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
                R.id.menuLogout -> logout()
            }
            true
        }
    }

    private fun logout() {
        viewPresenter.logout()
        startActivity(Intent(this, RequestLoginActivity::class.java))
        finish()
    }

    override fun getStringWithParams(stringRes: Int, currentValue: Int, maxValue: Int): String {
        return getString(stringRes, currentValue, maxValue)
    }

    override fun launchAlertView(error: ALERT_TYPE) {
        this.launchAlert(error)
    }

    override fun setNotification(language: String) = notification.setNotification(language)

    override fun updateNotification(language: String, progress: Int) = notification.updateNotification(language, progress)

    override fun cancelNotification(language: String) = notification.failedNotification(language)

    override fun completeNotification(language: String) = notification.completeNotification(language)

    override fun getLanguageList(): Array<String> = resources.getStringArray(R.array.language_array)

}
