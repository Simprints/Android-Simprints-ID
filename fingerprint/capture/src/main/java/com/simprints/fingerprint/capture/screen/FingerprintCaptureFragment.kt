package com.simprints.fingerprint.capture.screen

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.feature.exitform.toArgs
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.capture.databinding.FragmentFingerprintCaptureBinding
import com.simprints.fingerprint.capture.resources.buttonBackgroundColour
import com.simprints.fingerprint.capture.resources.buttonTextId
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.CollectFingerprintsState
import com.simprints.fingerprint.capture.views.confirmfingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.capture.views.fingerviewpager.FingerViewPagerManager
import com.simprints.fingerprint.capture.views.tryagainsplash.TryAnotherFingerSplashDialogFragment
import com.simprints.fingerprint.connect.FingerprintConnectContract
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.system.Vibrate
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FingerprintCaptureFragment : Fragment(R.layout.fragment_fingerprint_capture) {

    private val args: FingerprintCaptureFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentFingerprintCaptureBinding::bind)
    private val vm: FingerprintCaptureViewModel by viewModels()

    private lateinit var fingerViewPagerManager: FingerViewPagerManager
    private var confirmDialog: AlertDialog? = null
    private var hasSplashScreenBeenTriggered: Boolean = false

    @Inject
    lateinit var observeFingerprintScanStatus: ObserveFingerprintScanStatusUseCase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            AlertContract.DESTINATION
        ) { findNavController().finishWithResult(this, it) }

        findNavController().handleResult<ExitFormResult>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            ExitFormContract.DESTINATION,
        ) {
            if (it.submittedOption() != null) {
                findNavController().finishWithResult(this, it)
            }
        }

        findNavController().handleResult<Serializable>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            FingerprintConnectContract.DESTINATION
        ) {
            if (it !is FingerprintConnectResult || !it.isSuccess) {
                findNavController().finishWithResult(this, it)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            vm.handleOnBackPressed()
            if (vm.stateLiveData.value?.currentCaptureState()?.isCommunicating() != true) {
                openRefusal()
            }
        }
        observeBioSdkInit()

        vm.launchReconnect.observe(viewLifecycleOwner, LiveDataEventObserver { launchConnection() })

        vm.handleOnViewCreated(
            args.params.fingerprintsToCapture,
            args.params.fingerprintSDK,
        )
        initUI()
    }

    private fun initUI() {
        initToolbar(args.params.flowType)
        initMissingFingerButton()
        initViewPagerManager()
        initScanButton()
        observeStateChanges()
    }

    private fun observeBioSdkInit() {
        vm.invalidLicense.observe(viewLifecycleOwner, LiveDataEventObserver {
            findNavController().navigateSafely(
                this,
                R.id.action_fingerprintCaptureFragment_to_graphAlert,
                alertConfiguration {
                    color = AlertColor.Gray
                    titleRes = IDR.string.configuration_licence_invalid_title
                    messageRes = IDR.string.configuration_licence_invalid_message
                    image = IDR.drawable.ic_exclamation
                    leftButton = AlertButtonConfig.Close
                    appErrorReason = AppErrorReason.LICENSE_INVALID
                    eventType = AlertScreenEventType.LICENSE_INVALID
                }.toArgs()
            )
        })
    }

    private fun openRefusal() {
        findNavController().navigateSafely(
            this,
            R.id.action_fingerprintCaptureFragment_to_graphExitForm,
            exitFormConfiguration {
                titleRes = IDR.string.exit_form_title_fingerprinting
                backButtonRes =IDR.string.exit_form_continue_fingerprints_button
                visibleOptions = scannerOptions()
            }.toArgs()
        )
    }

    private fun initToolbar(flowType: FlowType) {
        binding.toolbar.title = when (flowType) {
            FlowType.ENROL -> getString(IDR.string.fingerprint_capture_enrol_title)
            FlowType.IDENTIFY -> getString(IDR.string.fingerprint_capture_identify_title)
            FlowType.VERIFY -> getString(IDR.string.fingerprint_capture_verify_title)
        }
    }

    private fun initViewPagerManager() {
        fingerViewPagerManager = FingerViewPagerManager(
            vm.stateLiveData.value?.fingerStates?.map { it.id }.orEmpty().toMutableList(),
            this,
            binding.fingerprintViewPager,
            binding.fingerprintIndicator,
            onFingerSelected = { position -> vm.updateSelectedFinger(position) },
            isAbleToSelectNewFinger = { vm.stateLiveData.value?.currentCaptureState()?.isCommunicating() != true }
        )
    }

    private fun initMissingFingerButton() {
        binding.fingerprintMissingFinger.paintFlags =
            binding.fingerprintMissingFinger.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.fingerprintMissingFinger.setOnClickListener {
            Simber.tag(FINGER_CAPTURE.name).i("Missing finger text clicked")
            vm.handleMissingFingerButtonPressed()
        }
    }

    private fun initScanButton() {
        binding.fingerprintScanButton.setOnClickListener {
            Simber.tag(FINGER_CAPTURE.name).i("Scan button clicked")
            vm.handleScanButtonPressed()
        }
    }

    private fun observeStateChanges() {
        vm.stateLiveData.observe(viewLifecycleOwner) { state ->
            if (state != null) {
                // Update pager
                fingerViewPagerManager.setCurrentPageAndFingerStates(state.fingerStates, state.currentFingerIndex)

                // Update button
                with(state.currentCaptureState()) {
                    binding.fingerprintScanButton.text = getString(buttonTextId(state.isAskingRescan))
                    binding.fingerprintScanButton.setBackgroundColor(resources.getColor(buttonBackgroundColour(), null))
                }

                updateConfirmDialog(state)
                updateSplashScreen(state)
            }
        }

        vm.vibrate.observe(viewLifecycleOwner, LiveDataEventObserver { Vibrate.vibrate(requireContext()) })

        vm.noFingersScannedToast.observe(viewLifecycleOwner, LiveDataEventObserver {
            requireContext().showToast(IDR.string.fingerprint_capture_no_fingers_scanned)
        })

        vm.launchAlert.observe(viewLifecycleOwner, LiveDataEventObserver {
            findNavController().navigateSafely(
                this,
                R.id.action_fingerprintCaptureFragment_to_graphAlert,
                alertConfiguration {
                    titleRes = IDR.string.fingerprint_capture_error_title
                    messageRes = IDR.string.fingerprint_capture_unexpected_error_message
                    color = AlertColor.Red
                    image = IDR.drawable.ic_alert_default
                    eventType = AlertScreenEventType.UNEXPECTED_ERROR
                    leftButton = AlertButtonConfig.Close
                }.toArgs()
            )
        })
        vm.finishWithFingerprints.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { fingerprints ->
            findNavController().finishWithResult(this, fingerprints)
        })
    }

    private fun launchConnection() {
        //If we exit from the ConnectScanner screen, we first resume the capture screen. This leads
        //to a crash because a second navigation to ConnectScanner is attempted but by the time it's
        // executed we are already on the exit screen.
        try {
            findNavController().navigateSafely(
                this,
                R.id.action_fingerprintCaptureFragment_to_graphConnectScanner,
                FingerprintConnectContract.getArgs(args.params.fingerprintSDK)
            )
        } catch (e: Exception) {
            Simber.tag(FINGER_CAPTURE.name).i("Error launching scanner connection screen", e)
        }
    }

    override fun onResume() {
        super.onResume()
        vm.handleOnResume()
        observeFingerprintScanStatus(
            viewLifecycleOwner.lifecycleScope,
            args.params.fingerprintSDK
        )
    }

    override fun onPause() {
        vm.handleOnPause()
        super.onPause()
        observeFingerprintScanStatus.stopObserving()
    }

    private fun updateConfirmDialog(state: CollectFingerprintsState) {
        confirmDialog = if (state.isShowingConfirmDialog && confirmDialog == null) {
            val dialogItems = state.fingerStates.map {
                ConfirmFingerprintsDialog.Item(
                    it.id,
                    it.captures.count { capture -> capture is CaptureState.ScanProcess.Collected && capture.scanResult.isGoodScan() },
                    it.captures.size
                )
            }
            ConfirmFingerprintsDialog(requireContext(), dialogItems,
                onConfirm = {
                    Simber.tag(FINGER_CAPTURE.name).i("Confirm fingerprints clicked")
                    vm.handleConfirmFingerprintsAndContinue()
                },
                onRestart = {
                    Simber.tag(FINGER_CAPTURE.name).i("Restart clicked")
                    vm.handleRestart()
                })
                .create().also { it.show() }
        } else if (!state.isShowingConfirmDialog) {
            confirmDialog?.let { if (it.isShowing) it.dismiss() }
            null
        } else {
            confirmDialog
        }
    }

    private fun updateSplashScreen(state: CollectFingerprintsState) {
        if (state.isShowingSplashScreen && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            if (!hasSplashScreenBeenTriggered) {
                TryAnotherFingerSplashDialogFragment().show(childFragmentManager, "splash")
                hasSplashScreenBeenTriggered = true
            }
        } else {
            hasSplashScreenBeenTriggered = false
        }
    }

    override fun onDestroyView() {
        confirmDialog?.dismiss()
        super.onDestroyView()
    }
}
