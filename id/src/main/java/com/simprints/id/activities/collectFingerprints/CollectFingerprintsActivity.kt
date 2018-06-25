package com.simprints.id.activities.collectFingerprints

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.annotation.DrawableRes
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.TimeoutBar
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import javax.inject.Inject

class CollectFingerprintsActivity :
    AppCompatActivity(),
    CollectFingerprintsContract.View,
    NavigationView.OnNavigationItemSelectedListener {

    override lateinit var viewPresenter: CollectFingerprintsContract.Presenter

    override var buttonContinue = false

    private var rightToLeft: Boolean = false

    override lateinit var viewPager: ViewPagerCustom
    override lateinit var indicatorLayout: LinearLayout
    override lateinit var pageAdapter: FingerPageAdapter
    override lateinit var timeoutBar: TimeoutBar
    override lateinit var un20WakeupDialog: ProgressDialog

    private var continueItem: MenuItem? = null
    private lateinit var syncItem: MenuItem

    // Singletons
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var dbManager: DbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as Application
        app.component.inject(this)

        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        nav_view.itemIconTintList = null

        viewPresenter = CollectFingerprintsPresenter(this, this)

        initBarAndDrawer()
        indicatorLayout = indicator_layout

        viewPresenter.start()
    }

    override fun onStart() {
        super.onStart()
        viewPresenter.handleOnStart()
        LanguageHelper.setLanguage(this, preferencesManager.language)
    }

    // init the bars and drawers, in particular:
    // 1) add listeners for drawer open/close
    // 2) set the title based on the CalloutAction
    private fun initBarAndDrawer() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        syncItem = nav_view.menu.findItem(R.id.nav_sync)

        nav_view.setNavigationItemSelectedListener(this)
        supportActionBar?.let {
            it.show()
            when (preferencesManager.calloutAction) {
                CalloutAction.REGISTER -> it.setTitle(R.string.register_title)
                CalloutAction.IDENTIFY -> it.setTitle(R.string.identify_title)
                CalloutAction.UPDATE -> it.setTitle(R.string.update_title)
                CalloutAction.VERIFY -> it.setTitle(R.string.verify_title)
                else -> viewPresenter.handleUnexpectedError(InvalidCalloutParameterError.forParameter("CalloutParameters"))
            }
        }
    }

    override fun setScanButtonListeners(onClick: () -> Unit, onLongClick: () -> Boolean) {
        scan_button.setOnClickListener { onClick() }
        scan_button.setOnLongClickListener { onLongClick() }
    }

    override fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean) {
        viewPager = view_pager
        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                onPageSelected(position)
            }
        })
        view_pager.setOnTouchListener { _, _ -> onTouch() }
        view_pager.currentItem = viewPresenter.currentActiveFingerNo

        // If the layout is from right to left, we need to reverse the scrolling direction
        if (rightToLeft) view_pager.rotationY = 180f
    }

    override fun refreshScanButtonAndTimeoutBar() {
        val activeStatus = viewPresenter.currentFinger().status
        scan_button.setText(activeStatus.buttonTextId)
        scan_button.setTextColor(activeStatus.buttonTextColor)
        scan_button.setBackgroundColor(activeStatus.buttonBgColor)

        timeoutBar.setProgressBar(activeStatus)
    }

    override fun refreshFingerFragment() {
        pageAdapter.getFragment(viewPresenter.currentActiveFingerNo)?.let {
            if (rightToLeft) {
                it.view?.rotationY = 180f
            }
            it.updateTextAccordingToStatus()
        }
    }

    override fun refreshContinueButton(nbCollected: Int, promptContinue: Boolean) {
        buttonContinue = false

        continueItem?.let {
            if (viewPresenter.isScanning()) {
                it.setIcon(R.drawable.ic_menu_forward_grey)
                it.isEnabled = false
            } else {
                if (nbCollected == 0) {
                    it.setIcon(R.drawable.ic_menu_forward_grey)
                } else if (nbCollected > 0 && promptContinue) {
                    it.setIcon(R.drawable.ic_menu_forward_green)
                    buttonContinue = true
                } else if (nbCollected > 0) {
                    it.setIcon(R.drawable.ic_menu_forward_white)
                }
                it.isEnabled = nbCollected > 0
            }
        }
    }

    // Swipes ViewPager automatically when the scanner's button is pressed.
    override fun nudgeMode() {
        val nudge = preferencesManager.nudgeMode

        if (nudge) {
            Handler().postDelayed({
                if (viewPresenter.currentActiveFingerNo < viewPresenter.activeFingers.size) {
                    view_pager.setScrollDuration(SLOW_SWIPE_SPEED)
                    view_pager.currentItem = viewPresenter.currentActiveFingerNo + 1
                    view_pager.setScrollDuration(FAST_SWIPE_SPEED)
                }
            }, AUTO_SWIPE_DELAY)
        }
    }

    override fun finishSuccessEnrol(result: Intent) =
        setResultAndFinish(Activity.RESULT_OK, result)

    override fun finishSuccessAndStartMatching(intent: Intent) =
        startActivityForResult(intent, CollectFingerprintsActivity.MATCHING_ACTIVITY_REQUEST_CODE)

    override fun finishFailure() =
        setResultAndFinish(Activity.RESULT_CANCELED)

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            viewPresenter.isScanning() -> viewPresenter.handleBackPressedWhileScanning()
            else -> {
                viewPresenter.handleOnBackPressedToLeave()
                startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        continueItem = menu.findItem(R.id.action_forward)
        viewPresenter.refreshDisplay()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_forward) {
            viewPresenter.onActionForward()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_autoAdd -> viewPresenter.handleAutoAddFingerPressed()
            R.id.nav_add -> viewPresenter.handleAddFingerPressed()
            R.id.nav_help -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            R.id.privacy -> startActivityForResult(Intent(this, PrivacyActivity::class.java), PRIVACY_ACTIVITY_REQUEST_CODE)
            R.id.nav_sync -> {
                viewPresenter.handleSyncPressed()
                return true
            }
            R.id.nav_about -> startActivityForResult(Intent(this, AboutActivity::class.java), ABOUT_ACTIVITY_REQUEST_CODE)
            R.id.nav_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_ACTIVITY_REQUEST_CODE)
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        when (requestCode) {
            SETTINGS_ACTIVITY_REQUEST_CODE,
            PRIVACY_ACTIVITY_REQUEST_CODE,
            ABOUT_ACTIVITY_REQUEST_CODE ->
                super.onActivityResult(requestCode, resultCode, data)
            REFUSAL_ACTIVITY_REQUEST, ALERT_ACTIVITY_REQUEST_CODE ->
                if (resultCode == RESULT_TRY_AGAIN)
                    viewPresenter.handleTryAgain()
                else
                    setResultAndFinish(resultCode, data)
            else -> setResultAndFinish(resultCode, data)
        }

    private fun setResultAndFinish(resultCode: Int, data: Intent? = null) {
        setResult(resultCode, data)
        finish()
    }

    override fun onStop() {
        super.onStop()
        viewPresenter.handleOnStop()
    }

    override fun setSyncItem(enabled: Boolean, title: String, @DrawableRes icon: Int) {
        runOnUiThreadIfStillRunning {
            syncItem.isEnabled = enabled
            syncItem.title = title
            syncItem.setIcon(icon)
        }
    }

    override fun setScanButtonEnabled(enabled: Boolean) {
        scan_button.isEnabled = enabled
    }

    override fun setCurrentViewPagerItem(idx: Int) {
        view_pager.currentItem = idx
    }

    override fun cancelAndFinish() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun doLaunchAlert(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    companion object {

        private const val AUTO_SWIPE_DELAY: Long = 500
        private const val FAST_SWIPE_SPEED = 100
        private const val SLOW_SWIPE_SPEED = 1000

        private const val ALERT_ACTIVITY_REQUEST_CODE = 0
        private const val MATCHING_ACTIVITY_REQUEST_CODE = 1
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 2
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 3
        private const val ABOUT_ACTIVITY_REQUEST_CODE = 4
    }
}
