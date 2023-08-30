package com.simprints.fingerprint.activities.collect

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ShowExitFormWrapper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.collect.confirmfingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.activities.collect.fingerviewpager.FingerViewPagerManager
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.resources.buttonBackgroundColour
import com.simprints.fingerprint.activities.collect.resources.buttonTextId
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.state.CaptureState.Collected
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.tryagainsplash.SplashScreenActivity
import com.simprints.fingerprint.activities.connect.ConnectScannerActivity
import com.simprints.fingerprint.activities.connect.request.ConnectScannerTaskRequest
import com.simprints.fingerprint.activities.refusal.RefusalAlertHelper
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.data.domain.fingerprint.Fingerprint
import com.simprints.fingerprint.databinding.ActivityCollectFingerprintsBinding
import com.simprints.fingerprint.databinding.ContentMainBinding
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForCollectFingerprintsActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.Vibrate
import com.simprints.fingerprint.tools.extensions.setResultAndFinish
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectFingerprintsActivity : FingerprintActivity() {

    @Inject
    lateinit var masterFlowManager: MasterFlowManager

    private val vm: CollectFingerprintsViewModel by viewModels()

    private val binding by viewBinding(ActivityCollectFingerprintsBinding::inflate)
    private lateinit var mainContentBinding: ContentMainBinding

    private lateinit var fingerViewPagerManager: FingerViewPagerManager
    private var confirmDialog: AlertDialog? = null
    private var hasSplashScreenBeenTriggered: Boolean = false

    private val showRefusal = registerForActivityResult(ShowExitFormWrapper()) { result ->
        RefusalAlertHelper.handleRefusal(
            result = result,
            onSubmit = { setResultAndFinish(ResultCode.REFUSED, it) },
        )
    }

    private val alertHelper = AlertActivityHelper()
    private val showAlert = registerForActivityResult(ShowAlertWrapper()) { data ->
        alertHelper.handleAlertResult(this, data,
            showRefusal = { showRefusal.launch(RefusalAlertHelper.refusalArgs()) },
            retry = {}
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        mainContentBinding = binding.mainContent

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val fingerprintRequest = this.intent.extras?.getParcelable<CollectFingerprintsTaskRequest>(
            CollectFingerprintsTaskRequest.BUNDLE_KEY
        ) ?: throw InvalidRequestForCollectFingerprintsActivityException()

        vm.start(fingerprintRequest.fingerprintsToCapture)

        initUiComponents()
        observeStateChanges()
    }

    private fun initUiComponents() {
        initToolbar()
        initViewPagerManager()
        initScanButton()
        initMissingFingerButton()
    }

    private fun initToolbar() {
        // TODO setSupportActionBar(binding.toolbar)
        binding.toolbar.title = when (masterFlowManager.getCurrentAction()) {
            Action.ENROL -> getString(R.string.register_title)
            Action.IDENTIFY -> getString(R.string.identify_title)
            Action.VERIFY -> getString(R.string.verify_title)
        }
    }

    private fun initViewPagerManager() {
        fingerViewPagerManager = FingerViewPagerManager(
            vm.state.fingerStates.map { it.id }.toMutableList(),
            this,
            mainContentBinding.viewPager,
            mainContentBinding.indicatorLayout,
            onFingerSelected = { position -> vm.updateSelectedFinger(position) },
            isAbleToSelectNewFinger = { !vm.state.currentCaptureState().isCommunicating() }
        )
    }

    private fun initMissingFingerButton() {
        mainContentBinding.missingFingerText.text = getString(R.string.missing_finger)
        mainContentBinding.missingFingerText.paintFlags =
            mainContentBinding.missingFingerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        mainContentBinding.missingFingerText.setOnClickListener {
            vm.logUiMessageForCrashReport("Missing finger text clicked")
            vm.handleMissingFingerButtonPressed()
        }
    }

    private fun initScanButton() {
        mainContentBinding.scanButton.setOnClickListener {
            vm.logUiMessageForCrashReport("Scan button clicked")
            vm.handleScanButtonPressed()
        }
    }

    private fun observeStateChanges() {
        vm.stateLiveData.activityObserveWith {
            it.updateViewPagerManager()
            it.updateScanButton()
            it.listenForConfirmDialog()
            it.listenForSplashScreen()
        }

        vm.vibrate.activityObserveEventWith { Vibrate.vibrate(this) }
        vm.noFingersScannedToast.activityObserveEventWith { showToast(getString(R.string.no_fingers_scanned)) }
        vm.launchAlert.activityObserveEventWith { showAlert.launch(it.toAlertConfig().toArgs()) }
        vm.launchReconnect.activityObserveEventWith { launchConnectScannerActivityForReconnect() }
        vm.finishWithFingerprints.activityObserveEventWith { setResultAndFinishSuccess(it) }
    }

    private fun CollectFingerprintsState.updateViewPagerManager() {
        fingerViewPagerManager.setCurrentPageAndFingerStates(fingerStates, currentFingerIndex)
    }

    private fun CollectFingerprintsState.updateScanButton() {
        with(currentCaptureState()) {
            mainContentBinding.scanButton.text = getString(buttonTextId(isAskingRescan))
            mainContentBinding.scanButton.setBackgroundColor(
                resources.getColor(
                    buttonBackgroundColour(),
                    null
                )
            )
        }
    }

    private fun CollectFingerprintsState.listenForConfirmDialog() {
        confirmDialog = if (isShowingConfirmDialog && confirmDialog == null) {
            val dialogItems = fingerStates.map {
                ConfirmFingerprintsDialog.Item(
                    it.id,
                    it.captures.count { capture -> capture is Collected && capture.scanResult.isGoodScan() },
                    it.captures.size
                )
            }
            ConfirmFingerprintsDialog(this@CollectFingerprintsActivity, dialogItems,
                callbackConfirm = {
                    vm.logUiMessageForCrashReport("Confirm fingerprints clicked")
                    vm.handleConfirmFingerprintsAndContinue()
                },
                callbackRestart = {
                    vm.logUiMessageForCrashReport("Restart clicked")
                    vm.handleRestart()
                })
                .create().also { it.show() }
        } else if (!isShowingConfirmDialog) {
            confirmDialog?.let { if (it.isShowing) it.dismiss() }
            null
        } else {
            confirmDialog
        }
    }

    private fun CollectFingerprintsState.listenForSplashScreen() {
        if (isShowingSplashScreen && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!hasSplashScreenBeenTriggered) {
                startActivity(
                    Intent(
                        this@CollectFingerprintsActivity,
                        SplashScreenActivity::class.java
                    )
                )
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                hasSplashScreenBeenTriggered = true
            }
        } else {
            hasSplashScreenBeenTriggered = false
        }
    }

    private fun launchConnectScannerActivityForReconnect() {
        val intent = Intent(this, ConnectScannerActivity::class.java).apply {
            putExtra(
                ConnectScannerTaskRequest.BUNDLE_KEY,
                ConnectScannerTaskRequest(ConnectScannerTaskRequest.ConnectMode.RECONNECT)
            )
        }
        startActivityForResult(intent, RequestCode.CONNECT.value)
    }

    private fun setResultAndFinishSuccess(fingerprints: List<Fingerprint>) {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(
                CollectFingerprintsTaskResult.BUNDLE_KEY,
                CollectFingerprintsTaskResult(fingerprints)
            )
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

    override fun onBackPressed() {
        vm.handleOnBackPressed()
        if (!vm.state.currentCaptureState().isCommunicating()) {
            showRefusal.launch(RefusalAlertHelper.refusalArgs())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        confirmDialog?.dismiss()
    }
}
