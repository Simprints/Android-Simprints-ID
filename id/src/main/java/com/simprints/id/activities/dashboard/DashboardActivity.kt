package com.simprints.id.activities.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.dashboard.views.WrapContentLinearLayoutManager
import com.simprints.id.activities.longConsent.LongConsentActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.activities.settings.SettingsActivity
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.content_dashboard.*
import javax.inject.Inject

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    @Inject
    lateinit var preferences: PreferencesManager
    @Inject
    lateinit var loginInfoManager: LoginInfoManager

    companion object {
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 1
        private const val LONG_CONSENT_ACTIVITY_REQUEST_CODE = 2
    }

    override lateinit var viewPresenter: DashboardContract.Presenter
    private lateinit var cardsViewAdapter: DashboardCardAdapter
    private var confirmationLogoutDialog: AlertDialog.Builder? = null
    private lateinit var notification: LongConsentNotification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val component = (application as Application).component
        component.inject(this)
        setSupportActionBar(dashboardToolbar)
        LanguageHelper.setLanguage(this, preferences.language)
        notification = LongConsentNotification(this)

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
        (dashboardCardsView as RecyclerView).also {
            it.setHasFixedSize(false)
            it.itemAnimator = DefaultItemAnimator()
            it.layoutManager = WrapContentLinearLayoutManager(this)
            it.adapter = cardsViewAdapter
        }
    }

    private fun initSwipeRefreshLayout(viewPresenter: DashboardContract.Presenter) {
        swipeRefreshLayout.setOnRefreshListener {
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
                R.id.menuPrivacyNotice -> startActivityForResult(Intent(this, LongConsentActivity::class.java), LONG_CONSENT_ACTIVITY_REQUEST_CODE)
                R.id.menuSettings -> startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
                R.id.menuLogout -> viewPresenter.userDidWantToLogout()
            }
            true
        }
    }

    override fun showConfirmationDialogForLogout() {
        confirmationLogoutDialog = AlertDialog.Builder(this)
            .setTitle(R.string.confirmation_logout_title)
            .setMessage(R.string.confirmation_logout_message)
            .setPositiveButton(getString(R.string.logout)) { _, _ -> logout() }
            .setNegativeButton(getString(R.string.confirmation_logout_cancel), null).apply { show() }
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
}
