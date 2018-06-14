package com.simprints.id.activities.collectFingerprints

import android.annotation.SuppressLint
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
import android.widget.ProgressBar
import android.widget.Toast
import com.simprints.id.Application
import com.simprints.id.R
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.PrivacyActivity
import com.simprints.id.activities.RefusalActivity
import com.simprints.id.activities.SettingsActivity
import com.simprints.id.activities.about.AboutActivity
import com.simprints.id.activities.matching.MatchingActivity
import com.simprints.id.controllers.Setup
import com.simprints.id.data.DataManager
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.domain.Finger.NB_OF_FINGERS
import com.simprints.id.domain.Finger.Status
import com.simprints.id.domain.FingerRes
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.tools.*
import com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN
import com.simprints.id.tools.extensions.isFingerNotCollectable
import com.simprints.id.tools.extensions.isFingerRequired
import com.simprints.id.tools.extensions.launchAlert
import com.simprints.id.tools.extensions.runOnUiThreadIfStillRunning
import com.simprints.libcommon.FingerConfig
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libcommon.ScanConfig
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.FingerIdentifier
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

    private var rightToLeft = false

    //Array with all fingers, built based on defaultScanConfig
    private var fingers = ArrayList<Finger>(NB_OF_FINGERS)

    //Array with only the active Fingers, used to populate the ViewPager
    private val activeFingers = ArrayList<Finger>()
    private var currentActiveFingerNo: Int = 0

    private val indicators = ArrayList<ImageView>()

    private lateinit var pageAdapter: FingerPageAdapter
    private lateinit var timeoutBar: TimeoutBar

    private var registrationResult: Registration? = null
    private var previousStatus: Status = Status.NOT_COLLECTED

    private var continueItem: MenuItem? = null
    private lateinit var syncItem: MenuItem

    override lateinit var un20WakeupDialog: ProgressDialog

    // Singletons
    @Inject lateinit var appState: AppState
    @Inject lateinit var setup: Setup
    @Inject lateinit var dataManager: DataManager
    @Inject lateinit var timeHelper: TimeHelper
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var dbManager: DbManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as Application
        app.component.inject(this)

        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        nav_view.itemIconTintList = null

        preferencesManager.msSinceBootOnMainStart = timeHelper.msSinceBoot()

        currentActiveFingerNo = 0

        pageAdapter = FingerPageAdapter(supportFragmentManager, activeFingers)

        timeoutBar = TimeoutBar(applicationContext,
            findViewById<View>(R.id.pb_timeout) as ProgressBar,
            preferencesManager.timeoutS * 1000)

        initBarAndDrawer()
        initIndicators()
        initViewPager()
        refreshDisplay()

        viewPresenter = CollectFingerprintsPresenter(this, this)
        viewPresenter.start()
    }

    override fun onStart() {
        super.onStart()
        viewPresenter.handleOnStart()
    }

    public override fun onResume() {
        super.onResume()
        LanguageHelper.setLanguage(this, preferencesManager.language)
    }

    // init the bars and drawers, in particular:
    // 1) add listeners for drawer open/close
    // 2) set the title based on the ColloutAction
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
                else -> handleUnexpectedError(InvalidCalloutParameterError.forParameter("CalloutParameters"))
            }
        }
    }

    // It adds an imageView for each bullet point (indicator) underneath the finger image.
    // "Indicator" indicates the scan state (good scan/bad scan/ etc...) for a specific finger.
    private fun initIndicators() {
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        // If the layout is from right to left, we need to reverse the scrolling direction
        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                currentActiveFingerNo = position
                refreshDisplay()
                appState.scanner.resetUI(null)
            }
        })
        view_pager.setOnTouchListener { _, _ -> viewPresenter.isScanning() }
        view_pager.currentItem = viewPresenter.currentActiveFingerNo

        if (rightToLeft) {
            view_pager.rotationY = 180f
        }
    }

    // Resets the UI:
    // 1) set the right image for the current finger indicator (gray/green bullet point)
    // 2) set the right UI for scan button based on current finger state.
    // 3) set the direction for the ViewPager swipe.
    // 4) set the UI for the top-right button (continueItem) based on the scan state.
    //      interesting: promptContinue - it drives the arrow's UI, but it depends on
    //                                    the last scan only.
    private fun refreshDisplay() {
        // Update indicators display
        var nbCollected = 0

        var promptContinue = true

        activeFingers.indices.forEach { fingerIndex ->
            val selected = currentActiveFingerNo == fingerIndex
            val finger = activeFingers[fingerIndex]
            indicators[fingerIndex].setImageResource(finger.status.getDrawableId(selected))

            if (finger.template != null) {
                nbCollected++
            }
            if (!finger.isGoodScan && !finger.isRescanGoodScan) {
                promptContinue = false
            }
        }

        // Update scan button display
        val activeStatus = activeFingers[currentActiveFingerNo].status
        scan_button.setText(activeStatus.buttonTextId)
        scan_button.setTextColor(activeStatus.buttonTextColor)
        scan_button.setBackgroundColor(activeStatus.buttonBgColor)

        timeoutBar.setProgressBar(activeStatus)

        pageAdapter.getFragment(currentActiveFingerNo)?.let {
            if (rightToLeft) {
                it.view?.rotationY = 180f
            }
            it.updateTextAccordingToStatus()
        }

        buttonContinue = false

        continueItem?.let {
            if (activeFingers[currentActiveFingerNo].isCollecting) {
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

    private fun handleUnexpectedError(error: SimprintsError) {
        analyticsManager.logError(error)
        doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    // Swipes ViewPager automatically when the scanner's button is pressed.
    private fun nudgeMode() {
        val nudge = preferencesManager.nudgeMode

        if (nudge) {
            Handler().postDelayed({
                if (currentActiveFingerNo < activeFingers.size) {
                    view_pager.setScrollDuration(SLOW_SWIPE_SPEED)
                    view_pager.currentItem = currentActiveFingerNo + 1
                    view_pager.setScrollDuration(FAST_SWIPE_SPEED)
                }
            }, AUTO_SWIPE_DELAY)
        }
    }

    // Executed either user presses the scanner button at the end of the scans or the top-right arrow
    // It gathers all valid fingerprints and either it saves them in case of enrol or returns them
    // for identifications/verifications
    override fun onActionForward() {
        // Gathers the fingerprints in a list
        activeFingers[currentActiveFingerNo]

        val fingerprints = ArrayList<Fingerprint>()
        var nbRequiredFingerprints = 0

        for (finger in activeFingers) {
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
        registrationResult = Registration(preferencesManager.patientId)

        val resultData = Intent(Constants.SIMPRINTS_REGISTER_INTENT)
        FormatResult.put(resultData, registrationResult, preferencesManager.resultFormat)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

    // If the enrol fails, the activity shows an alert activity the finishes.
    private fun handleRegistrationFailure(throwable: Throwable) {
        analyticsManager.logError(SimprintsError(throwable))
        doLaunchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> drawer.closeDrawer(GravityCompat.START)
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
        refreshDisplay()
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
        val id = item.itemId

        when (id) {
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
