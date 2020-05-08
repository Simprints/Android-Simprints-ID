package com.simprints.fingerprint.activities.collect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.collect.confirmfingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.activities.collect.fingerviewpager.FingerPageAdapter
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.resources.*
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.activities.collect.tryagainsplash.SplashScreenActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForCollectFingerprintsActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.launchRefusalActivity
import com.simprints.fingerprint.tools.extensions.setResultAndFinish
import com.simprints.fingerprint.tools.extensions.showToast
import kotlinx.android.synthetic.main.activity_collect_fingerprints.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CollectFingerprintsActivity : FingerprintActivity() {

    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private val masterFlowManager: MasterFlowManager by inject()

    private val vm: CollectFingerprintsViewModel by viewModel()

    private lateinit var pageAdapter: FingerPageAdapter
    private lateinit var timeoutBar: ScanningTimeoutBar
    private var confirmDialog: AlertDialog? = null
    private val indicators = ArrayList<ImageView>()
    private var rightToLeft: Boolean = false
    private var numberOfFingersCurrentlyDisplayed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_fingerprints)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val fingerprintRequest = this.intent.extras?.getParcelable<CollectFingerprintsTaskRequest>(CollectFingerprintsTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForCollectFingerprintsActivityException()

        vm.start(fingerprintRequest.fingerprintsToCapture)

        initUiComponents()
        observeStateChanges()
    }

    private fun initUiComponents() {
        configureRightToLeft()
        initToolbar()
        initPageAdapter()
        initViewPager()
        initIndicators()
        initTimeoutBar()
        initScanButton()
        initMissingFingerButton()
    }

    private fun configureRightToLeft() {
        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        supportActionBar?.title = when (masterFlowManager.getCurrentAction()) {
            Action.ENROL -> androidResourcesHelper.getString(R.string.register_title)
            Action.IDENTIFY -> androidResourcesHelper.getString(R.string.identify_title)
            Action.VERIFY -> androidResourcesHelper.getString(R.string.verify_title)
        }
    }

    private fun initMissingFingerButton() {
        with(androidResourcesHelper) {
            missingFingerText.text = getString(R.string.missing_finger)
            missingFingerText.paintFlags = missingFingerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        missingFingerText.setOnClickListener {
            vm.logUiMessageForCrashReport("Missing finger text clicked")
            vm.handleMissingFingerButtonPressed()
        }
    }

    private fun initScanButton() {
        scan_button.setOnClickListener {
            vm.logUiMessageForCrashReport("Scan button clicked")
            vm.handleScanButtonPressed()
        }
    }

    private fun initTimeoutBar() {
        timeoutBar = if (vm.isImageTransferRequired()) {
            ScanningWithImageTransferTimeoutBar(pb_timeout, CollectFingerprintsViewModel.scanningTimeoutMs, CollectFingerprintsViewModel.imageTransferTimeoutMs)
        } else {
            ScanningOnlyTimeoutBar(pb_timeout, CollectFingerprintsViewModel.scanningTimeoutMs)
        }
    }

    private fun initIndicators() {
        indicator_layout.removeAllViewsInLayout()
        indicators.clear()
        vm.state().orderedFingers().forEachIndexed { index, _ ->
            val indicator = ImageView(this)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener {
                if (!vm.state().currentFingerState().isCommunicating()) {
                    vm.updateSelectedFinger(index)
                }
            }
            indicators.add(indicator)
            indicator_layout.addView(indicator, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun initPageAdapter() {
        pageAdapter = FingerPageAdapter(
            supportFragmentManager,
            vm.state().orderedFingers()
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                vm.updateSelectedFinger(position)
            }
        })
        view_pager.setOnTouchListener { _, _ -> vm.state().currentFingerState().isCommunicating() }

        // If the layout is from right to left, we need to reverse the scrolling direction
        if (rightToLeft) view_pager.rotationY = 180f
    }

    private fun observeStateChanges() {
        vm.state.activityObserveWith {
            it.updateNumberOfFingersCurrentlyDisplayed()
            it.updateIndicators()
            it.updateScanButton()
            it.updateViewPager()
            it.updateProgressBar()
            it.listenForConfirmDialog()
            it.listenForSplashScreen()
        }

        vm.vibrate.activityObserveEventWith { Vibrate.vibrate(this) }
        vm.noFingersScannedToast.activityObserveEventWith { showToast(androidResourcesHelper.getString(R.string.no_fingers_scanned)) }
        vm.launchRefusal.activityObserveEventWith { launchRefusalActivity() }
        vm.launchAlert.activityObserveEventWith { launchAlert(this, it) }
        vm.launchReconnect.activityObserveEventWith { launchConnectScannerActivityForReconnect() }
        vm.finishWithFingerprints.activityObserveEventWith { setResultAndFinishSuccess(it) }
    }

    private fun CollectFingerprintsState.updateNumberOfFingersCurrentlyDisplayed() {
        val numberOfFingersToDisplay = orderedFingers().size
        if (numberOfFingersToDisplay != numberOfFingersCurrentlyDisplayed) {
            initIndicators()
            initPageAdapter()
            view_pager.adapter = pageAdapter
            pageAdapter.notifyDataSetChanged()
        }
        numberOfFingersCurrentlyDisplayed = numberOfFingersToDisplay
    }

    private fun CollectFingerprintsState.updateIndicators() {
        orderedFingers().forEachIndexed { index, finger ->
            val selected = currentFingerIndex == index
            indicators[index].setImageResource(fingerStates.getValue(finger).indicatorDrawableId(selected))
        }
    }

    private fun CollectFingerprintsState.updateScanButton() {
        with(currentFingerState()) {
            scan_button.text = androidResourcesHelper.getString(buttonTextId(isAskingRescan))
            scan_button.setTextColor(resources.getColor(buttonTextColour(), null))
            scan_button.setBackgroundColor(resources.getColor(buttonBackgroundColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateViewPager() {
        view_pager.currentItem = currentFingerIndex

        // If the layout is has been rotated for RtL, we need to rotate the fragment back so it's upright
        if (rightToLeft) {
            pageAdapter.getFragment(currentFingerIndex)?.view?.rotationY = 180f
        }
    }

    private fun CollectFingerprintsState.updateProgressBar() {
        when (val fingerState = currentFingerState()) {
            FingerCollectionState.NotCollected,
            FingerCollectionState.Skipped -> {
                timeoutBar.handleCancelled()
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bar)
            }
            is FingerCollectionState.Scanning -> timeoutBar.startTimeoutBar()
            is FingerCollectionState.TransferringImage -> timeoutBar.handleScanningFinished()
            is FingerCollectionState.NotDetected -> {
                timeoutBar.handleCancelled()
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
            }
            is FingerCollectionState.Collected -> if (fingerState.scanResult.isGoodScan()) {
                timeoutBar.handleCancelled()
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_good)
            } else {
                timeoutBar.handleCancelled()
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
            }
        }
    }

    private fun CollectFingerprintsState.listenForConfirmDialog() {
        confirmDialog = if (isShowingConfirmDialog && confirmDialog == null) {
            val mapOfScannedFingers = orderedFingers().associate { finger ->
                val fingerState = fingerStates[finger]
                androidResourcesHelper.getString(finger.nameTextId()) to
                    (fingerState is FingerCollectionState.Collected && fingerState.scanResult.isGoodScan())
            }
            ConfirmFingerprintsDialog(this@CollectFingerprintsActivity, androidResourcesHelper, mapOfScannedFingers,
                callbackConfirm = {
                    vm.logUiMessageForCrashReport("Confirm fingerprints clicked")
                    vm.handleConfirmFingerprintsAndContinue()
                },
                callbackRestart = {
                    vm.logUiMessageForCrashReport("Restart clicked")
                    vm.handleRestart()
                })
                .create().also { it.show() }
        } else {
            confirmDialog?.let { if (it.isShowing) it.dismiss() }
            null
        }
    }

    private fun CollectFingerprintsState.listenForSplashScreen() {
        if (isShowingSplashScreen && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            startActivity(Intent(this@CollectFingerprintsActivity, SplashScreenActivity::class.java))
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    private fun launchConnectScannerActivityForReconnect() {
        val intent = Intent(this, ConnectScannerActivity::class.java).apply {
            putExtra(ConnectScannerTaskRequest.BUNDLE_KEY, ConnectScannerTaskRequest(ConnectScannerTaskRequest.ConnectMode.RECONNECT))
        }
        startActivityForResult(intent, RequestCode.CONNECT.value)
    }

    private fun setResultAndFinishSuccess(fingerprints: List<Fingerprint>) {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(CollectFingerprintsTaskResult.BUNDLE_KEY, CollectFingerprintsTaskResult(fingerprints))
        })
    }

    override fun onResume() {
        super.onResume()
        vm.handleOnResume()
    }

    override fun onPause() {
        super.onPause()
        vm.handleOnPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                }
            }
        }
    }

    override fun onBackPressed() {
        vm.handleOnBackPressed()
    }
}
