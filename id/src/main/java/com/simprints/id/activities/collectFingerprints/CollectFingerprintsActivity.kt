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
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.*
import com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Registration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class CollectFingerprintsActivity :
    AppCompatActivity(),
    CollectFingerprintsContract.View,
    NavigationView.OnNavigationItemSelectedListener {

    override lateinit var viewPresenter: CollectFingerprintsContract.Presenter

    override var buttonContinue = false

    private var rightToLeft: Boolean = false

    private val indicators = ArrayList<ImageView>()

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

    // It adds an imageView for each bullet point (indicator) underneath the finger image.
    // "Indicator" indicates the scan state (good scan/bad scan/ etc...) for a specific finger.
    override fun initIndicators() {
        indicator_layout.removeAllViewsInLayout()
        indicators.clear()
        for (i in viewPresenter.activeFingers.indices) {
            val indicator = ImageView(this)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { view_pager.currentItem = i }
            indicators.add(indicator)
            indicator_layout.addView(indicator, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    override fun setScanButtonListeners(onClick: () -> Unit, onLongClick: () -> Boolean) {
        scan_button.setOnClickListener { onClick() }
        scan_button.setOnLongClickListener { onLongClick() }
    }

    override fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean) {
        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) { onPageSelected(position) }
        })
        view_pager.setOnTouchListener { _, _ -> onTouch() }
        view_pager.currentItem = viewPresenter.currentActiveFingerNo

        // If the layout is from right to left, we need to reverse the scrolling direction
        if (rightToLeft) view_pager.rotationY = 180f
    }

    override fun refreshIndicators(): Pair<Int, Boolean> {
        var nbCollected = 0

        var promptContinue = true

        viewPresenter.activeFingers.indices.forEach { fingerIndex ->
            val selected = viewPresenter.currentActiveFingerNo == fingerIndex
            val finger = viewPresenter.activeFingers[fingerIndex]
            indicators[fingerIndex].setImageResource(finger.status.getDrawableId(selected))

            if (finger.template != null) {
                nbCollected++
            }
            if (!finger.isGoodScan && !finger.isRescanGoodScan) {
                promptContinue = false
            }
        }
        return Pair(nbCollected, promptContinue)
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

    // Executed either user presses the scanner button at the end of the scans or the top-right arrow
    // It gathers all valid fingerprints and either it saves them in case of enrol or returns them
    // for identifications/verifications
    override fun onActionForward() {

        val fingerprints = ArrayList<Fingerprint>()
        var nbRequiredFingerprints = 0

        for (finger in viewPresenter.activeFingers) {
            if ((finger.isGoodScan || finger.isBadScan || finger.isRescanGoodScan) && finger.template != null) {
                fingerprints.add(Fingerprint(finger.id, finger.template.templateBytes))

                nbRequiredFingerprints++
            }
        }

        if (nbRequiredFingerprints < 1) {
            Toast.makeText(this, "Please scan at least 1 required finger", Toast.LENGTH_LONG).show()
        } else {
            val person = Person(preferencesManager.patientId, fingerprints)
            if (preferencesManager.calloutAction === CalloutAction.REGISTER || preferencesManager.calloutAction === CalloutAction.UPDATE) {
                dbManager.savePerson(person)
                        .subscribe({
                            preferencesManager.lastEnrolDate = Date()
                            handleRegistrationSuccess()
                        }) { throwable -> handleRegistrationFailure(throwable) }
            } else {
                val intent = Intent(this, MatchingActivity::class.java)
                intent.putExtra(IntentKeys.matchingActivityProbePersonKey, person)
                startActivityForResult(intent, MATCHING_ACTIVITY_REQUEST_CODE)
            }
        }
    }

    // If the enrol succeed, the activity returns the result and finishes.
    private fun handleRegistrationSuccess() {
        val registrationResult = Registration(preferencesManager.patientId)
        val resultData = Intent(Constants.SIMPRINTS_REGISTER_INTENT)
        FormatResult.put(resultData, registrationResult, preferencesManager.resultFormat)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

    // If the enrol fails, the activity shows an alert activity the finishes.
    private fun handleRegistrationFailure(throwable: Throwable) {
        viewPresenter.handleUnexpectedError(SimprintsError(throwable))
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

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
            onActionForward()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_autoAdd -> {
                viewPresenter.handleAutoAddFingerPressed()
                return true
            }
            R.id.nav_add -> {
                viewPresenter.handleAddFingerPressed()
                return true
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SETTINGS_ACTIVITY_REQUEST_CODE, PRIVACY_ACTIVITY_REQUEST_CODE, ABOUT_ACTIVITY_REQUEST_CODE -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
            REFUSAL_ACTIVITY_REQUEST, ALERT_ACTIVITY_REQUEST_CODE -> if (resultCode == RESULT_TRY_AGAIN) {
                viewPresenter.handleTryAgain()
            } else {
                setResult(resultCode, data)
                finish()
            }
            else -> {
                setResult(resultCode, data)
                finish()
            }
        }
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
