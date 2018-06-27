package com.simprints.id.activities.collectFingerprints

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.simprints.id.R
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN
import com.simprints.id.tools.TimeoutBar
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class CollectFingerprintsActivity :
    AppCompatActivity(),
    CollectFingerprintsContract.View,
    NavigationView.OnNavigationItemSelectedListener {

    override lateinit var viewPresenter: CollectFingerprintsContract.Presenter

    override var buttonContinue = false
    override lateinit var viewPager: ViewPagerCustom
    override lateinit var indicatorLayout: LinearLayout
    override lateinit var pageAdapter: FingerPageAdapter
    override lateinit var scanButton: Button
    override lateinit var timeoutBar: TimeoutBar
    override lateinit var un20WakeupDialog: ProgressDialog
    override lateinit var syncItem: MenuItem

    private var rightToLeft: Boolean = false
    private var continueItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        configureRightToLeft()

        viewPresenter = CollectFingerprintsPresenter(this, this)
        initBar()
        initDrawer()
        initViewFields()
        viewPresenter.start()
    }

    private fun configureRightToLeft() {
        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    private fun initBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        supportActionBar?.title = viewPresenter.getTitle()
    }

    private fun initDrawer() {
        nav_view.itemIconTintList = null
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun initViewFields() {
        viewPager = view_pager
        syncItem = nav_view.menu.findItem(R.id.nav_sync)
        indicatorLayout = indicator_layout
        scanButton = scan_button
    }

    override fun onStart() {
        super.onStart()
        viewPresenter.handleOnStart()
    }

    override fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean) {
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

        reverseViewPagerIfNeeded()
    }

    private fun reverseViewPagerIfNeeded() {
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
            reverseFingerFragmentIfNeeded(it)
            it.updateTextAccordingToStatus()
        }
    }

    private fun reverseFingerFragmentIfNeeded(it: FingerFragment) {
        // If the layout direction is RTL, then the view pager will have been rotated,
        // but the image and text need to be rotated back
        if (rightToLeft) {
            it.view?.rotationY = 180f
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

    override fun finishSuccessEnrol(result: Intent) =
        setResultAndFinish(Activity.RESULT_OK, result)

    override fun finishSuccessAndStartMatching(intent: Intent) =
        startActivityForResult(intent, CollectFingerprintsActivity.MATCHING_ACTIVITY_REQUEST_CODE)

    override fun cancelAndFinish() =
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
                    viewPresenter.handleTryAgainFromDifferentActivity()
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

    override fun doLaunchAlert(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    companion object {
        private const val ALERT_ACTIVITY_REQUEST_CODE = 0
        private const val MATCHING_ACTIVITY_REQUEST_CODE = 1
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 2
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 3
        private const val ABOUT_ACTIVITY_REQUEST_CODE = 4
    }
}
