package com.simprints.fingerprint.capture.screen

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.common.FlowProvider
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.feature.exitform.toArgs
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertError
import com.simprints.fingerprint.capture.resources.buttonBackgroundColour
import com.simprints.fingerprint.capture.resources.buttonTextId
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.CollectFingerprintsState
import com.simprints.fingerprint.capture.FingerprintCaptureResult
import com.simprints.fingerprint.capture.views.confirmfingerprints.ConfirmFingerprintsDialog
import com.simprints.fingerprint.capture.views.fingerviewpager.FingerViewPagerManager
import com.simprints.fingerprint.connect.FingerprintConnectContract
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.fingerprint.data.domain.fingerprint.fromDomainToModuleApi
import com.simprints.fingerprint.data.domain.fingerprint.fromModuleApiToDomain
import com.simprints.fingerprint.databinding.FragmentFingerprintCaptureBinding
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.system.Vibrate
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FingerprintCaptureFragment : Fragment(R.layout.fragment_fingerprint_capture) {

    private val args: FingerprintCaptureFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentFingerprintCaptureBinding::bind)
    private val vm: FingerprintCaptureViewModel by activityViewModels()

    private lateinit var fingerViewPagerManager: FingerViewPagerManager
    private var confirmDialog: AlertDialog? = null
    // TODO private var hasSplashScreenBeenTriggered: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().handleResult<AlertResult>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            AlertContract.DESTINATION
        ) {
            val alertError = it.payload.getString(AlertError.PAYLOAD_KEY)?.let { AlertError.valueOf(it) }
                ?: AlertError.UNEXPECTED_ERROR

            when (it.buttonKey) {
                AlertError.ACTION_CLOSE -> findNavController().finishWithResult(this, it)
                AlertContract.ALERT_BUTTON_PRESSED_BACK -> {
                    if (alertError == AlertError.UNEXPECTED_ERROR) {
                        findNavController().finishWithResult(this, it)
                    } else {
                        openRefusal()
                    }
                }
            }
        }

        findNavController().handleResult<ExitFormResult>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            ExitFormContract.DESTINATION,
        ) {
            if (it.submittedOption() != null) {
                findNavController().finishWithResult(this, it)
            }
        }

        findNavController().handleResult<FingerprintConnectResult>(
            viewLifecycleOwner,
            R.id.fingerprintCaptureFragment,
            FingerprintConnectContract.DESTINATION
        ) { startCollection() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            vm.handleOnBackPressed()
            if (vm.stateLiveData.value?.currentCaptureState()?.isCommunicating() != true) {
                openRefusal()
            }
        }

        if (vm.hasScanner()) startCollection() else launchConnection()
    }

    private fun startCollection() {
        // TODO simplify methods params
        vm.start(args.params.fingerprintsToCapture.map { it.fromModuleApiToDomain() })

        initToolbar(args.params.flowType)
        initMissingFingerButton()
        initViewPagerManager()
        initScanButton()
        observeStateChanges()
    }

    private fun openRefusal() {
        findNavController().navigate(
            R.id.action_fingerprintCaptureFragment_to_graphExitForm,
            exitFormConfiguration {
                titleRes = com.simprints.infra.resources.R.string.why_did_you_skip_fingerprinting
                backButtonRes = com.simprints.infra.resources.R.string.button_scan_prints
                visibleOptions = scannerOptions()
            }.toArgs()
        )
    }

    private fun initToolbar(flowType: FlowProvider.FlowType) {
        binding.toolbar.title = when (flowType) {
            FlowProvider.FlowType.ENROL -> getString(IDR.string.register_title)
            FlowProvider.FlowType.IDENTIFY -> getString(IDR.string.identify_title)
            FlowProvider.FlowType.VERIFY -> getString(IDR.string.verify_title)
        }
    }

    private fun initViewPagerManager() {
        fingerViewPagerManager = FingerViewPagerManager(
            vm.stateLiveData.value?.fingerStates?.map { it.id }.orEmpty().toMutableList(),
            this,
            binding.fingerprintViewPager,
            binding.fingerprintIndicator,
            onFingerSelected = { position -> vm.updateSelectedFinger(position) },
            isAbleToSelectNewFinger = { !vm.state.currentCaptureState().isCommunicating() }
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

                listenForConfirmDialog(state)
                // TODO it.listenForSplashScreen()
            }
        }

        vm.vibrate.observe(viewLifecycleOwner, LiveDataEventObserver { Vibrate.vibrate(requireContext()) })

        vm.noFingersScannedToast.observe(viewLifecycleOwner, LiveDataEventObserver {
            requireContext().showToast(IDR.string.no_fingers_scanned)
        })

        vm.launchAlert.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            findNavController().navigate(R.id.action_fingerprintCaptureFragment_to_graphAlert, it.toAlertConfig().toArgs())
        })
        vm.launchReconnect.observe(viewLifecycleOwner, LiveDataEventObserver { launchConnection() })
        vm.finishWithFingerprints.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { fingerprints ->
            findNavController().finishWithResult(this, FingerprintCaptureResult(
                // TODO move the mapping to the vm
                fingerprints.map { fingerprint ->
                    FingerprintCaptureResult.Item(
                        identifier = fingerprint.fingerId.fromDomainToModuleApi(),
                        sample = FingerprintCaptureResult.Sample(
                            fingerIdentifier = fingerprint.fingerId.fromDomainToModuleApi(),
                            template = fingerprint.templateBytes,
                            templateQualityScore = fingerprint.qualityScore,
                            imageRef = fingerprint.imageRef
                                ?.let { FingerprintCaptureResult.Path(it.path.parts) }
                                ?.let { FingerprintCaptureResult.SecuredImageRef(it) },
                            format = fingerprint.format,
                        )
                    )
                }
            ))
        })
    }

    private fun launchConnection() {
        findNavController().navigate(
            R.id.action_fingerprintCaptureFragment_to_graphConnectScanner,
            FingerprintConnectContract.getArgs(true)
        )
    }

    override fun onResume() {
        super.onResume()
        vm.handleOnResume()
    }

    override fun onPause() {
        vm.handleOnPause()
        super.onPause()
    }

    private fun listenForConfirmDialog(state: CollectFingerprintsState) {
        confirmDialog = if (state.isShowingConfirmDialog && confirmDialog == null) {
            val dialogItems = state.fingerStates.map {
                ConfirmFingerprintsDialog.Item(
                    it.id,
                    it.captures.count { capture -> capture is CaptureState.Collected && capture.scanResult.isGoodScan() },
                    it.captures.size
                )
            }
            ConfirmFingerprintsDialog(requireContext(), dialogItems,
                onConfirm = {
                    vm.logUiMessageForCrashReport("Confirm fingerprints clicked")
                    vm.handleConfirmFingerprintsAndContinue()
                },
                onRestart = {
                    vm.logUiMessageForCrashReport("Restart clicked")
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

    override fun onDestroyView() {
        confirmDialog?.dismiss()
        super.onDestroyView()
    }
}
