package com.simprints.id.activities.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.simprints.id.controllers.SetupCallback
import com.simprints.id.data.DataManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Finger
import com.simprints.id.domain.Finger.NB_OF_FINGERS
import com.simprints.id.domain.Finger.Status
import com.simprints.id.domain.FingerRes
import com.simprints.id.exceptions.unsafe.InvalidCalloutParameterError
import com.simprints.id.exceptions.unsafe.SimprintsError
import com.simprints.id.exceptions.unsafe.UnexpectedScannerError
import com.simprints.id.services.sync.SyncService
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
import com.simprints.libscanner.ButtonListener
import com.simprints.libscanner.SCANNER_ERROR
import com.simprints.libscanner.ScannerCallback
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.FingerIdentifier
import com.simprints.libsimprints.Registration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var buttonContinue = false

    private var rightToLeft = false

    private val scannerButtonListener = ButtonListener {
        if (buttonContinue)
            onActionForward()
        else if (!activeFingers[currentActiveFingerNo].isGoodScan)
            toggleContinuousCapture()
    }

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

    private lateinit var un20WakeupDialog: ProgressDialog
    private lateinit var dataManager: DataManager
    private lateinit var syncHelper: MainActivitySyncHelper

    private val defaultScanConfig = ScanConfig().apply {
        set(FingerIdentifier.LEFT_THUMB, FingerConfig.REQUIRED, 0, 0)
        set(FingerIdentifier.LEFT_INDEX_FINGER, FingerConfig.REQUIRED, 1, 1)
        set(FingerIdentifier.LEFT_3RD_FINGER, FingerConfig.OPTIONAL, 4, 2)
        set(FingerIdentifier.LEFT_4TH_FINGER, FingerConfig.OPTIONAL, 5, 3)
        set(FingerIdentifier.LEFT_5TH_FINGER, FingerConfig.OPTIONAL, 6, 4)
        set(FingerIdentifier.RIGHT_THUMB, FingerConfig.OPTIONAL, 2, 5)
        set(FingerIdentifier.RIGHT_INDEX_FINGER, FingerConfig.OPTIONAL, 3, 6)
        set(FingerIdentifier.RIGHT_3RD_FINGER, FingerConfig.OPTIONAL, 7, 7)
        set(FingerIdentifier.RIGHT_4TH_FINGER, FingerConfig.OPTIONAL, 8, 8)
        set(FingerIdentifier.RIGHT_5TH_FINGER, FingerConfig.OPTIONAL, 9, 9)
    }

    // Singletons
    private lateinit var appState: AppState
    private lateinit var setup: Setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as Application
        dataManager = app.dataManager
        appState = app.appState
        setup = app.setup
        val syncClient = SyncService.getClient(this)
        val timeHelper = app.timeHelper

        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        nav_view.itemIconTintList = null

        dataManager.msSinceBootOnMainStart = timeHelper.msSinceBoot()

        currentActiveFingerNo = 0

        pageAdapter = FingerPageAdapter(supportFragmentManager, activeFingers)
        un20WakeupDialog = initUn20Dialog()
        timeoutBar = TimeoutBar(applicationContext,
            findViewById<View>(R.id.pb_timeout) as ProgressBar,
            dataManager.timeoutS * 1000)

        setFingerStatus()
        initActiveFingers()
        initBarAndDrawer()
        initIndicators()
        initScanButton()
        initViewPager()
        syncHelper = MainActivitySyncHelper(this, dataManager, syncClient, syncItem)
        refreshDisplay()
    }

    override fun onStart() {
        super.onStart()
        appState.scanner.registerButtonListener(scannerButtonListener)
    }

    public override fun onResume() {
        super.onResume()
        LanguageHelper.setLanguage(this, dataManager.language)
    }

    // Reads the fingerStatus Map (from sharedPrefs) and "active" LEFT_THUMB and LEFT_INDEX_FINGER as
    // default finger.
    private fun setFingerStatus() {
        // We set the two defaults in the config for the first reset.
        val fingerStatus = dataManager.fingerStatus as MutableMap
        fingerStatus[FingerIdentifier.LEFT_THUMB] = true
        fingerStatus[FingerIdentifier.LEFT_INDEX_FINGER] = true
        dataManager.fingerStatus = fingerStatus
    }

    // Builds the array of "fingers" and "activeFingers" based on the info from:
    // FingerIdentifier values - all possible fingers
    // defaultScanConfig - to find out if a finger is required or not, collectable or not, etc..
    // fingerStatusPersist - to find out if a finger is active or not (added by user with "Add finger dialog" or defaults ones)
    private fun initActiveFingers() {
        FingerIdentifier.values().take(NB_OF_FINGERS).forEachIndexed { _, identifier ->

            val wasFingerAddedByUser = { dataManager.fingerStatusPersist && dataManager.fingerStatus[identifier] == true }
            val isFingerRequired = { defaultScanConfig.isFingerRequired(identifier) }
            val isFingerActive = isFingerRequired() || wasFingerAddedByUser()

            val finger = Finger(identifier, isFingerActive, defaultScanConfig.getPriority(identifier), defaultScanConfig.getOrder(identifier))
            fingers.add(finger)
            if (isFingerActive) {
                activeFingers.add(finger)
            }
        }

        activeFingers.sort()
        fingers.sort()

        updateLastFinger()
    }

    // Creates a progress dialog when the scan gets disconnected
    private fun initUn20Dialog(): ProgressDialog {
        val dialog = ProgressDialog(this)
        dialog.isIndeterminate = true
        dialog.setCanceledOnTouchOutside(false)
        dialog.setMessage(getString(R.string.reconnecting_message))
        dialog.setOnCancelListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
        return dialog
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
            when (dataManager.calloutAction) {
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
        val indicatorLayout = findViewById<LinearLayout>(R.id.indicator_layout)
        indicatorLayout.removeAllViewsInLayout()
        indicators.clear()
        for (i in activeFingers.indices) {
            val indicator = ImageView(this)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener { view_pager.currentItem = i }
            indicators.add(indicator)
            indicatorLayout.addView(indicator, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
    }

    // Init the scan button listener:
    // Long click: it resets the state of the button (required for?)
    // Single tap: it triggers the scan
    private fun initScanButton() {
        scan_button.setOnClickListener { toggleContinuousCapture() }
        scan_button.setOnLongClickListener {
            if (!activeFingers[currentActiveFingerNo].isCollecting) {
                activeFingers[currentActiveFingerNo].isNotCollected
                activeFingers[currentActiveFingerNo].template = null
                refreshDisplay()
            }
            true
        }
    }

    // it start/stop the scan based on the activeFingers[currentActiveFingerNo] state
    private fun toggleContinuousCapture() {
        val finger = activeFingers[currentActiveFingerNo]

        when (finger.status) {
            Finger.Status.GOOD_SCAN -> {
                activeFingers[currentActiveFingerNo].isRescanGoodScan
                refreshDisplay()
            }
            Finger.Status.RESCAN_GOOD_SCAN, Finger.Status.BAD_SCAN, Finger.Status.NOT_COLLECTED -> {
                previousStatus = finger.status
                finger.status = Status.COLLECTING
                refreshDisplay()
                scan_button.isEnabled = true
                refreshDisplay()
                startContinuousCapture()
            }
            Finger.Status.COLLECTING -> stopContinuousCapture()
        }
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
        view_pager.setOnTouchListener { _, _ -> activeFingers[currentActiveFingerNo].isCollecting }
        view_pager.currentItem = currentActiveFingerNo

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

    private fun resetUIFromError() {
        activeFingers[currentActiveFingerNo].status = Status.NOT_COLLECTED
        activeFingers[currentActiveFingerNo].template = null

        appState.scanner.resetUI(object : ScannerCallback {
            override fun onSuccess() {
                refreshDisplay()
                scan_button.isEnabled = true
                un20WakeupDialog.dismiss()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                when (scanner_error) {
                    SCANNER_ERROR.BUSY -> resetUIFromError()
                    SCANNER_ERROR.INVALID_STATE -> reconnect()
                    else -> handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "MainActivity"))
                }
            }
        })
    }

    private fun handleUnexpectedError(error: SimprintsError) {
        dataManager.logError(error)
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

    // Swipes ViewPager automatically when the scanner's button is pressed.
    private fun nudgeMode() {
        val nudge = dataManager.nudgeMode

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
    private fun onActionForward() {
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
            val person = Person(dataManager.patientId, fingerprints)
            if (dataManager.calloutAction === CalloutAction.REGISTER || dataManager.calloutAction === CalloutAction.UPDATE) {
                dataManager.savePerson(person)
                    .subscribe({
                        dataManager.lastEnrolDate = Date()
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
        registrationResult = Registration(dataManager.patientId)

        val resultData = Intent(Constants.SIMPRINTS_REGISTER_INTENT)
        FormatResult.put(resultData, registrationResult, dataManager.resultFormat)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

    // If the enrol fails, the activity shows an alert activity the finishes.
    private fun handleRegistrationFailure(throwable: Throwable) {
        dataManager.logError(SimprintsError(throwable))
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> drawer.closeDrawer(GravityCompat.START)
            activeFingers[currentActiveFingerNo].isCollecting -> toggleContinuousCapture()
            else -> {
                setup.stop()
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
                if (activeFingers[currentActiveFingerNo].isCollecting) {
                    toggleContinuousCapture()
                }
                autoAdd()
            }
            R.id.nav_add -> {
                if (activeFingers[currentActiveFingerNo].isCollecting) {
                    toggleContinuousCapture()
                }
                addFinger()
            }
            R.id.nav_help -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            R.id.privacy -> startActivityForResult(Intent(this, PrivacyActivity::class.java), PRIVACY_ACTIVITY_REQUEST_CODE)
            R.id.nav_sync -> {
                syncHelper.sync(dataManager)
                return true
            }
            R.id.nav_about -> startActivityForResult(Intent(this, AboutActivity::class.java),
                ABOUT_ACTIVITY_REQUEST_CODE)
            R.id.nav_settings -> startActivityForResult(Intent(this, SettingsActivity::class.java),
                SETTINGS_ACTIVITY_REQUEST_CODE)
        }
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun addFinger() {
        val fingerOptions = arrayListOf<FingerDialogOption>().apply {
            fingers.forEach {
                val fingerName = getString(FingerRes.get(it).nameId)
                FingerDialogOption(fingerName, it.id, defaultScanConfig.isFingerRequired(it.id), it.isActive).also {
                    this.add(it)
                }
            }
        }

        val dialog = AddFingerDialog(this, fingerOptions, dataManager.fingerStatusPersist) { persistFingerState, fingersDialogOptions ->
            val currentActiveFinger = activeFingers[currentActiveFingerNo]

            val persistentFingerStatus = dataManager.fingerStatus as MutableMap

            //updateFingersActiveState
            fingersDialogOptions.forEach { option ->
                fingers.find { it.id == option.fingerId }?.isActive = option.active
            }
            dataManager.fingerStatusPersist = persistFingerState

            //remove old active fingers from MainView
            fingers
                .filter { it.isActive && !activeFingers.contains(it) }
                .forEach {
                    activeFingers.add(it)
                    persistentFingerStatus[it.id] = true
                }

            //add new active fingers from MainView
            fingers
                .filter { !it.isActive && activeFingers.contains(it) }
                .forEach {
                    activeFingers.remove(it)
                    persistentFingerStatus[it.id] = false
                }

            dataManager.fingerStatus = persistentFingerStatus
            activeFingers.sort()

            currentActiveFingerNo = if (currentActiveFinger.isActive) {
                activeFingers.indexOf(currentActiveFinger)
            } else {
                0
            }

            updateLastFinger()

            initIndicators()
            pageAdapter.notifyDataSetChanged()
            view_pager.currentItem = currentActiveFingerNo
            refreshDisplay()
        }.create()

        runOnUiThreadIfStillRunning {
            dialog.show()
        }
    }

    private fun updateLastFinger() {
        fingers.forEach { it.isLastFinger = false }
        fingers.last().isLastFinger = true
    }

    private fun autoAdd() {
        activeFingers[activeFingers.size - 1].isLastFinger = false

        // Construct a list of fingers sorted by priority
        val fingersSortedByPriority = arrayOfNulls<Finger>(NB_OF_FINGERS)
        for (finger in fingers) {
            fingersSortedByPriority[finger.priority] = finger
        }

        // Auto-add the next finger sorted by the "priority" field
        for (finger in fingersSortedByPriority.filterNotNull()) {
            if (!defaultScanConfig.isFingerNotCollectable(finger.id) && !activeFingers.contains(finger)) {
                activeFingers.add(finger)
                finger.isActive = true
                break
            }
        }
        activeFingers.sort()

        activeFingers[activeFingers.size - 1].isLastFinger = true

        initIndicators()
        pageAdapter.notifyDataSetChanged()
        view_pager.currentItem = currentActiveFingerNo
        refreshDisplay()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            SETTINGS_ACTIVITY_REQUEST_CODE, PRIVACY_ACTIVITY_REQUEST_CODE, ABOUT_ACTIVITY_REQUEST_CODE -> {
                appState.scanner.registerButtonListener(scannerButtonListener)
                super.onActivityResult(requestCode, resultCode, data)
            }

            REFUSAL_ACTIVITY_REQUEST, ALERT_ACTIVITY_REQUEST_CODE -> if (resultCode == RESULT_TRY_AGAIN) {
                reconnect()
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

    override fun onPause() {
        super.onPause()
        syncHelper.syncManager.stopListeners()
    }

    override fun onStop() {
        super.onStop()
        val scanner = appState.scanner
        scanner?.unregisterButtonListener(scannerButtonListener)
    }

    private fun startContinuousCapture() {
        timeoutBar.startTimeoutBar()

        appState.scanner.startContinuousCapture(dataManager.qualityThreshold,
            (dataManager.timeoutS * 1000).toLong(), object : ScannerCallback {
            override fun onSuccess() {
                timeoutBar.stopTimeoutBar()
                captureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                if (scanner_error == SCANNER_ERROR.TIMEOUT)
                    forceCapture()
                else handleError(scanner_error)
            }
        })
    }

    private fun stopContinuousCapture() {
        appState.scanner.stopContinuousCapture()
    }

    private fun forceCapture() {
        appState.scanner.forceCapture(dataManager.qualityThreshold, object : ScannerCallback {
            override fun onSuccess() {
                captureSuccess()
            }

            override fun onFailure(scanner_error: SCANNER_ERROR) {
                handleError(scanner_error)
            }
        })
    }

    /**
     * For hardware version <=4, set bad scan if force capture isn't possible
     */
    private fun forceCaptureNotPossible() {
        activeFingers[currentActiveFingerNo].status = Status.BAD_SCAN
        Vibrate.vibrate(this@MainActivity, dataManager.vibrateMode)
        refreshDisplay()
    }

    private fun cancelCaptureUI() {
        activeFingers[currentActiveFingerNo].status = previousStatus
        timeoutBar.cancelTimeoutBar()
        refreshDisplay()
    }

    private fun captureSuccess() {
        val finger = activeFingers[currentActiveFingerNo]
        val quality = appState.scanner.imageQuality

        if (finger.template == null || finger.template.qualityScore < quality) {
            try {
                activeFingers[currentActiveFingerNo].template = Fingerprint(
                    finger.id,
                    appState.scanner.template)
                // TODO : change exceptions in libcommon
            } catch (ex: IllegalArgumentException) {
                dataManager.logError(SimprintsError("IllegalArgumentException in MainActivity.captureSuccess()"))
                resetUIFromError()
                return
            }
        }

        val qualityScore1 = dataManager.qualityThreshold

        if (quality >= qualityScore1) {
            activeFingers[currentActiveFingerNo].status = Status.GOOD_SCAN
            nudgeMode()
        } else {
            activeFingers[currentActiveFingerNo].status = Status.BAD_SCAN
        }

        Vibrate.vibrate(this@MainActivity, dataManager.vibrateMode)
        refreshDisplay()
    }

    private fun handleError(scanner_error: SCANNER_ERROR) {
        when (scanner_error) {
            SCANNER_ERROR.BUSY, SCANNER_ERROR.INTERRUPTED, SCANNER_ERROR.TIMEOUT -> cancelCaptureUI()

            SCANNER_ERROR.OUTDATED_SCANNER_INFO -> {
                cancelCaptureUI()
                appState.scanner.updateSensorInfo(object : ScannerCallback {
                    override fun onSuccess() {
                        resetUIFromError()
                    }

                    override fun onFailure(scanner_error: SCANNER_ERROR) {
                        handleError(scanner_error)
                    }
                })
            }

            SCANNER_ERROR.INVALID_STATE, SCANNER_ERROR.SCANNER_UNREACHABLE, SCANNER_ERROR.UN20_INVALID_STATE -> {
                cancelCaptureUI()
                reconnect()
            }

            SCANNER_ERROR.UN20_SDK_ERROR -> forceCaptureNotPossible()

            SCANNER_ERROR.IO_ERROR, SCANNER_ERROR.NO_RESPONSE, SCANNER_ERROR.UNEXPECTED, SCANNER_ERROR.BLUETOOTH_DISABLED, SCANNER_ERROR.BLUETOOTH_NOT_SUPPORTED, SCANNER_ERROR.SCANNER_UNBONDED, SCANNER_ERROR.UN20_FAILURE, SCANNER_ERROR.UN20_LOW_VOLTAGE -> {
                cancelCaptureUI()
                handleUnexpectedError(UnexpectedScannerError.forScannerError(scanner_error, "MainActivity"))
            }
        }
    }

    private fun reconnect() {
        appState.scanner.unregisterButtonListener(scannerButtonListener)

        val setupCallback = object : SetupCallback {
            override fun onSuccess() {
                Log.d(this@MainActivity, "reconnect.onSuccess()")
                un20WakeupDialog.dismiss()
                appState.scanner.registerButtonListener(scannerButtonListener)
            }

            override fun onProgress(progress: Int, detailsId: Int) {
                Log.d(this@MainActivity, "reconnect.onProgress()")
            }

            override fun onError(resultCode: Int) {
                Log.d(this@MainActivity, "reconnect.onError()")
                un20WakeupDialog.dismiss()
                launchAlert(ALERT_TYPE.DISCONNECTED)
            }

            override fun onAlert(alertType: ALERT_TYPE) {
                Log.d(this@MainActivity, "reconnect.onAlert()")
                un20WakeupDialog.dismiss()
                launchAlert(alertType)
            }
        }

        runOnUiThreadIfStillRunning {
            un20WakeupDialog.show()
        }

        setup.start(this, setupCallback)
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
