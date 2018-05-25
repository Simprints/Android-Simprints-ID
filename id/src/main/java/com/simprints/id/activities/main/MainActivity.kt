package com.simprints.id.activities.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
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
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var buttonContinue = false

    private var rightToLeft = false

    private val scannerButtonListener = ButtonListener {
        if (buttonContinue)
            onActionForward()
        else if (activeFingers[currentActiveFingerNo].status != Status.GOOD_SCAN)
            toggleContinuousCapture()
    }

    private var fingers = arrayOfNulls<Finger>(NB_OF_FINGERS)
    private val activeFingers = ArrayList<Finger>()
    private var currentActiveFingerNo: Int = 0

    private val indicators = ArrayList<ImageView>()

    private lateinit var pageAdapter: FingerPageAdapter
    private lateinit var timeoutBar: TimeoutBar

    private var registrationResult: Registration? = null
    private var previousStatus: Status = Status.NOT_COLLECTED

    private lateinit var continueItem: MenuItem
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

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.itemIconTintList = null

        dataManager.msSinceBootOnMainStart = timeHelper.msSinceBoot()

        fingers = arrayOfNulls(NB_OF_FINGERS)
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

    private fun setFingerStatus() {
        // We set the two defaults in the config for the first reset.
        val fingerStatus = dataManager.fingerStatus as MutableMap
        fingerStatus[FingerIdentifier.LEFT_THUMB] = true
        fingerStatus[FingerIdentifier.LEFT_INDEX_FINGER] = true
        dataManager.fingerStatus = fingerStatus
    }

    private fun initActiveFingers() {
        val fingerIdentifiers = FingerIdentifier.values()
        if (dataManager.fingerStatusPersist) {
            val fingerStatus = dataManager.fingerStatus
            for (i in 0 until NB_OF_FINGERS) {
                val id = fingerIdentifiers[i]
                fingerStatus[id]?.let {
                    fingers[i] = Finger(id, it, defaultScanConfig.getPriority(id), defaultScanConfig.getOrder(id))
                }
            }
        } else {
            for (i in 0 until NB_OF_FINGERS) {
                val id = fingerIdentifiers[i]
                fingers[i] = Finger(id, defaultScanConfig.get(id) == FingerConfig.REQUIRED, defaultScanConfig.getPriority(id), defaultScanConfig.getOrder(id))
            }
        }

        for (i in 0 until NB_OF_FINGERS) {
            fingers[i]?.let {
                if (it.isActive) {
                    activeFingers.add(it)
                }
            }
        }
        activeFingers.sort()
        activeFingers[activeFingers.size - 1].isLastFinger = true
        Arrays.sort(fingers)
    }

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

    private fun initBarAndDrawer() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        syncItem = navigationView.menu.findItem(R.id.nav_sync)

        navigationView.setNavigationItemSelectedListener(this)

        val actionBar = supportActionBar

        actionBar!!.show()

        when (dataManager.calloutAction) {
            CalloutAction.REGISTER -> actionBar.setTitle(R.string.register_title)
            CalloutAction.IDENTIFY -> actionBar.setTitle(R.string.identify_title)
            CalloutAction.UPDATE -> actionBar.setTitle(R.string.update_title)
            CalloutAction.VERIFY -> actionBar.setTitle(R.string.verify_title)
            else -> handleUnexpectedError(InvalidCalloutParameterError.forParameter("CalloutParameters"))
        }
    }

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

    private fun initScanButton() {
        scan_button.setOnClickListener { toggleContinuousCapture() }
        scan_button.setOnLongClickListener {
            if (activeFingers[currentActiveFingerNo].status != Status.COLLECTING) {
                activeFingers[currentActiveFingerNo].status = Status.NOT_COLLECTED
                activeFingers[currentActiveFingerNo].template = null
                refreshDisplay()
            }
            true
        }
    }

    private fun toggleContinuousCapture() {
        val finger = activeFingers[currentActiveFingerNo]

        when (activeFingers[currentActiveFingerNo].status) {
            Finger.Status.GOOD_SCAN -> {
                activeFingers[currentActiveFingerNo].status = Status.RESCAN_GOOD_SCAN
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
        view_pager.setOnTouchListener { _, _ -> activeFingers[currentActiveFingerNo].status == Status.COLLECTING }
        view_pager.currentItem = currentActiveFingerNo

        if (rightToLeft) {
            view_pager.rotationY = 180f
        }
    }

    private fun refreshDisplay() {
        // Update indicators display
        var nbCollected = 0

        var promptContinue = true

        for (i in activeFingers.indices) {
            val selected = currentActiveFingerNo == i
            val finger = activeFingers[i]
            indicators[i].setImageResource(finger.status.getDrawableId(selected))

            if (finger.template != null) {
                nbCollected++
            }
            if (finger.status != Status.GOOD_SCAN && finger.status != Status.RESCAN_GOOD_SCAN) {
                promptContinue = false
            }
        }

        // Update scan button display
        val activeStatus = activeFingers[currentActiveFingerNo].status
        scan_button.setText(activeStatus.buttonTextId)
        scan_button.setTextColor(activeStatus.buttonTextColor)
        scan_button.setBackgroundColor(activeStatus.buttonBgColor)

        timeoutBar.setProgressBar(activeStatus)

        val fragment = pageAdapter.getFragment(currentActiveFingerNo)
        if (fragment != null) {
            if (rightToLeft && fragment.view != null) {
                fragment.view!!.rotationY = 180f
            }
            fragment.updateTextAccordingToStatus()
        }

        buttonContinue = false

        if (activeFingers[currentActiveFingerNo].status == Status.COLLECTING) {
            continueItem.setIcon(R.drawable.ic_menu_forward_grey)
            continueItem.isEnabled = false
        } else {
            if (nbCollected == 0) {
                continueItem.setIcon(R.drawable.ic_menu_forward_grey)
            } else if (nbCollected > 0 && promptContinue) {
                continueItem.setIcon(R.drawable.ic_menu_forward_green)
                buttonContinue = true
            } else if (nbCollected > 0) {
                continueItem.setIcon(R.drawable.ic_menu_forward_white)
            }
            continueItem.isEnabled = nbCollected > 0
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

    /**
     * Start alert activity
     */
    private fun launchAlert(alertType: ALERT_TYPE) {
        AlertLauncher(this).launch(alertType, ALERT_ACTIVITY_REQUEST_CODE)
    }

    private fun handleUnexpectedError(error: SimprintsError) {
        dataManager.logError(error)
        launchAlert(ALERT_TYPE.UNEXPECTED_ERROR)
    }

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

    private fun onActionForward() {
        // Gathers the fingerprints in a list
        activeFingers[currentActiveFingerNo]

        val fingerprints = ArrayList<Fingerprint>()
        var nbRequiredFingerprints = 0

        for (finger in activeFingers) {
            if ((finger.status == Status.GOOD_SCAN ||
                            finger.status == Status.BAD_SCAN ||
                            finger.status == Status.RESCAN_GOOD_SCAN) && finger.template != null) {
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

    private fun handleRegistrationSuccess() {
        registrationResult = Registration(dataManager.patientId)

        val resultData = Intent(Constants.SIMPRINTS_REGISTER_INTENT)
        FormatResult.put(resultData, registrationResult, dataManager.resultFormat)
        setResult(Activity.RESULT_OK, resultData)
        finish()
    }

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
            activeFingers[currentActiveFingerNo].status == Status.COLLECTING -> toggleContinuousCapture()
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
                if (activeFingers[currentActiveFingerNo].status == Status.COLLECTING) {
                    toggleContinuousCapture()
                }
                autoAdd()
            }
            R.id.nav_add -> {
                if (activeFingers[currentActiveFingerNo].status == Status.COLLECTING) {
                    toggleContinuousCapture()
                }
                addFinger()
            }
            R.id.nav_help -> Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
            R.id.privacy -> startActivityForResult(Intent(this, PrivacyActivity::class.java),
                    PRIVACY_ACTIVITY_REQUEST_CODE)
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
        val checked = BooleanArray(fingers.size + 1)
        val labels = arrayOfNulls<String>(fingers.size + 1)
        for (finger in fingers.filterNotNull()) {
            val index = fingers.indexOf(finger)
            checked[index] = finger.isActive
            labels[index] = getString(FingerRes.get(finger).nameId)
        }
        labels[fingers.size] = getString(R.string.persistence_label)
        checked[fingers.size] = dataManager.fingerStatusPersist
        val builder = AlertDialog.Builder(this)
                .setTitle("Add Finger(s)")
                .setMultiChoiceItems(labels, checked, DialogInterface.OnMultiChoiceClickListener { dialogInterface, i, isChecked ->
                    if (i == fingers.size) {
                        checked[i] = isChecked
                        dataManager.fingerStatusPersist = isChecked
                        return@OnMultiChoiceClickListener
                    }

                    val finger = fingers[i]
                    finger?.let {
                        when (defaultScanConfig.get(it.id)) {
                            FingerConfig.DO_NOT_COLLECT -> {
                                checked[i] = false
                                (dialogInterface as AlertDialog).listView.setItemChecked(i, false)
                            }
                            FingerConfig.OPTIONAL -> {
                                checked[i] = isChecked
                                finger.isActive = isChecked
                            }
                            FingerConfig.REQUIRED -> {
                                checked[i] = true
                                (dialogInterface as AlertDialog).listView.setItemChecked(i, true)
                            }
                        }
                    }
                })
                .setPositiveButton(R.string.ok) { _, _ ->
                    val currentActiveFinger = activeFingers[currentActiveFingerNo]
                    activeFingers[activeFingers.size - 1].isLastFinger = false
                    val fingerStatus = dataManager.fingerStatus as MutableMap

                    for (finger in fingers.filterNotNull()) {
                        if (finger.isActive && !activeFingers.contains(finger)) {
                            activeFingers.add(finger)
                            if (dataManager.fingerStatusPersist)
                                fingerStatus[finger.id] = true
                        }
                        if (!finger.isActive && activeFingers.contains(finger)) {
                            activeFingers.remove(finger)
                            if (dataManager.fingerStatusPersist)
                                fingerStatus[finger.id] = false
                        }
                    }
                    dataManager.fingerStatus = fingerStatus
                    activeFingers.sort()

                    currentActiveFingerNo = if (currentActiveFinger.isActive) {
                        activeFingers.indexOf(currentActiveFinger)
                    } else {
                        0
                    }
                    activeFingers[activeFingers.size - 1].isLastFinger = true

                    initIndicators()
                    pageAdapter.notifyDataSetChanged()
                    view_pager.currentItem = currentActiveFingerNo
                    refreshDisplay()
                }
        if (!this.isFinishing) {
            builder.create().show()
        }
    }

    private fun autoAdd() {
        activeFingers[activeFingers.size - 1].isLastFinger = false

        // Construct a list of fingers sorted by priority
        val fingersSortedByPriority = arrayOfNulls<Finger>(NB_OF_FINGERS)
        for (finger in fingers.filterNotNull()) {
            fingersSortedByPriority[finger.priority] = finger
        }

        // Auto-add the next finger sorted by the "priority" field
        for (finger in fingersSortedByPriority.filterNotNull()) {
            if (defaultScanConfig.get(finger.id) != FingerConfig.DO_NOT_COLLECT && !activeFingers.contains(finger)) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
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
        }
        )
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

        if (!this.isFinishing) {
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
