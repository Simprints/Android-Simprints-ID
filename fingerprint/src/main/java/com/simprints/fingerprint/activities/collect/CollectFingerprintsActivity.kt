package com.simprints.fingerprint.activities.collect

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.Lifecycle
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.collect.confirmfingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.activities.collect.fingerviewpager.FingerViewPagerManager
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.resources.buttonBackgroundColour
import com.simprints.fingerprint.activities.collect.resources.buttonTextColour
import com.simprints.fingerprint.activities.collect.resources.buttonTextId
import com.simprints.fingerprint.activities.collect.resources.nameTextId
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.CaptureState.*
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.activities.collect.tryagainsplash.SplashScreenActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
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

    private val masterFlowManager: MasterFlowManager by inject()

    private val vm: CollectFingerprintsViewModel by viewModel()

    private lateinit var fingerViewPagerManager: FingerViewPagerManager
    private lateinit var timeoutBar: ScanningTimeoutBar
    private var confirmDialog: AlertDialog? = null
    private var hasSplashScreenBeenTriggered: Boolean = false

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
        initToolbar()
        initViewPagerManager()
        initTimeoutBar()
        initScanButton()
        initMissingFingerButton()
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        supportActionBar?.title = when (masterFlowManager.getCurrentAction()) {
            Action.ENROL -> getString(R.string.register_title)
            Action.IDENTIFY -> getString(R.string.identify_title)
            Action.VERIFY -> getString(R.string.verify_title)
        }
    }

    private fun initViewPagerManager() {
        fingerViewPagerManager = FingerViewPagerManager(
            vm.state().fingerStates.map { it.id }.toMutableList(),
            this,
            view_pager,
            indicator_layout,
            onFingerSelected = { position -> vm.updateSelectedFinger(position) },
            isAbleToSelectNewFinger = { !vm.state().currentCaptureState().isCommunicating() }
        )
    }

    private fun initMissingFingerButton() {

        missingFingerText.text = getString(R.string.missing_finger)
        missingFingerText.paintFlags = missingFingerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
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

    private fun observeStateChanges() {
        vm.state.activityObserveWith {
            it.updateViewPagerManager()
            it.updateScanButton()
            it.updateProgressBar()
            it.listenForConfirmDialog()
            it.listenForSplashScreen()
        }

        vm.vibrate.activityObserveEventWith { Vibrate.vibrate(this) }
        vm.noFingersScannedToast.activityObserveEventWith { showToast(getString(R.string.no_fingers_scanned)) }
        vm.launchAlert.activityObserveEventWith { launchAlert(this, it) }
        vm.launchReconnect.activityObserveEventWith { launchConnectScannerActivityForReconnect() }
        vm.finishWithFingerprints.activityObserveEventWith { setResultAndFinishSuccess(it) }
    }

    private fun CollectFingerprintsState.updateViewPagerManager() {
        fingerViewPagerManager.setCurrentPageAndFingerStates(fingerStates, currentFingerIndex)
    }

    private fun CollectFingerprintsState.updateScanButton() {
        with(currentCaptureState()) {
            scan_button.text = getString(buttonTextId(isAskingRescan))
            scan_button.setTextColor(resources.getColor(buttonTextColour(), null))
            scan_button.setBackgroundColor(resources.getColor(buttonBackgroundColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateProgressBar() {
        with(timeoutBar) {
            when (val fingerState = currentCaptureState()) {
                is NotCollected,
                is Skipped -> {
                    handleCancelled()
                    progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bar)
                }
                is Scanning -> startTimeoutBar()
                is TransferringImage -> handleScanningFinished()
                is NotDetected -> {
                    handleCancelled()
                    progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
                }
                is Collected -> if (fingerState.scanResult.isGoodScan()) {
                    handleCancelled()
                    progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_good)
                } else {
                    handleCancelled()
                    progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
                }
            }
        }
    }

    private fun CollectFingerprintsState.listenForConfirmDialog() {
        confirmDialog = if (isShowingConfirmDialog && confirmDialog == null) {
            val mapOfScannedFingers = fingerStates.associate { fingerState ->
                val currentCapture = fingerState.currentCapture()
                getString(fingerState.id.nameTextId()) to
                    (currentCapture is Collected && currentCapture.scanResult.isGoodScan())
            }
            ConfirmFingerprintsDialog(this@CollectFingerprintsActivity, mapOfScannedFingers,
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
            if (!hasSplashScreenBeenTriggered) {
                startActivity(Intent(this@CollectFingerprintsActivity, SplashScreenActivity::class.java))
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                hasSplashScreenBeenTriggered = true
            }
        } else {
            hasSplashScreenBeenTriggered = false
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
        if (!vm.state().currentCaptureState().isCommunicating()) {
            launchRefusalActivity()
        }
    }
}
