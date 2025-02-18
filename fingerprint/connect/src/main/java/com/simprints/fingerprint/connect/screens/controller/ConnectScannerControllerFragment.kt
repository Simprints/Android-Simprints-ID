package com.simprints.fingerprint.connect.screens.controller

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.domain.permission.worstPermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hasPermissions
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.feature.alert.AlertContract
import com.simprints.feature.alert.AlertResult
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel.ConnectScannerIssueScreen
import com.simprints.fingerprint.connect.screens.alert.AlertActivityHelper
import com.simprints.fingerprint.connect.screens.alert.AlertError
import com.simprints.fingerprint.connect.screens.issues.scanneroff.ScannerOffFragmentArgs
import com.simprints.fingerprint.connect.screens.ota.OtaFragmentArgs
import com.simprints.fingerprint.connect.screens.ota.OtaFragmentParams
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.FINGER_CAPTURE
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.system.Vibrate
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ConnectScannerControllerFragment : Fragment(R.layout.fragment_connect_scanner_controller) {
    private var shouldRequestPermissions = true

    private val args: ConnectScannerControllerFragmentArgs by navArgs()
    private val activityViewModel: ConnectScannerViewModel by activityViewModels()
    private val fragmentViewModel: ConnectScannerControllerViewModel by viewModels()

    private val alertHelper = AlertActivityHelper()

    private var knownScannedDialog: AlertDialog? = null

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissionsCall = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions: Map<String, Boolean> ->
        val permission = permissions
            .filterKeys(bluetoothPermissions::contains)
            .map { (permission, isGranted) ->
                requireActivity().permissionFromResult(permission, isGranted)
            }.worstPermissionStatus()

        Simber.i("Bluetooth permission: $permission", tag = FINGER_CAPTURE)

        when (permission) {
            PermissionStatus.Granted -> activityViewModel.connect()
            PermissionStatus.Denied -> requestBluetoothPermissions()
            PermissionStatus.DeniedNeverAskAgain -> activityViewModel.handleNoBluetoothPermission()
        }
    }

    private val hostFragment: Fragment?
        get() = childFragmentManager.findFragmentById(R.id.connect_scanner_host_fragment)

    private val internalNavController: NavController?
        get() = hostFragment?.findNavController()

    private val currentlyDisplayedInternalFragment: Fragment?
        get() = hostFragment?.childFragmentManager?.fragments?.first()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        Simber.i("ConnectScannerControllerFragment started", tag = ORCHESTRATION)

        shouldRequestPermissions = savedInstanceState?.getBoolean(KEY_SHOULD_REQUEST_PERMISSIONS)
            ?: shouldRequestPermissions

        if (!fragmentViewModel.isInitialized) {
            activityViewModel.init(args.params)
            fragmentViewModel.isInitialized = true
        }

        findNavController().handleResult(
            lifecycleOwner = this,
            currentDestinationId = R.id.connectScannerControllerFragment,
            targetDestinationId = ExitFormContract.DESTINATION,
            handler = ::handleExitForm,
        )
        findNavController().handleResult(
            lifecycleOwner = this,
            currentDestinationId = R.id.connectScannerControllerFragment,
            targetDestinationId = AlertContract.DESTINATION,
            handler = ::handleResult,
        )

        activityViewModel.showScannerIssueScreen.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { screen ->
                when (screen) {
                    ConnectScannerIssueScreen.BluetoothNoPermission -> showAlert(AlertError.BLUETOOTH_NO_PERMISSION)
                    ConnectScannerIssueScreen.BluetoothNotSupported -> showAlert(AlertError.BLUETOOTH_NOT_SUPPORTED)
                    ConnectScannerIssueScreen.LowBattery -> showAlert(AlertError.LOW_BATTERY)
                    ConnectScannerIssueScreen.UnexpectedError -> showAlert(AlertError.UNEXPECTED_ERROR)

                    ConnectScannerIssueScreen.ExitForm -> showExitForm()

                    ConnectScannerIssueScreen.BluetoothOff -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.issueBluetoothOffFragment,
                    )

                    ConnectScannerIssueScreen.NfcOff -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.issueNfcOffFragment,
                    )

                    ConnectScannerIssueScreen.NfcPair -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.issueNfcPairFragment,
                    )

                    ConnectScannerIssueScreen.SerialEntryPair -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.issueSerialEntryPairFragment,
                    )

                    is ConnectScannerIssueScreen.ScannerOff -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.issueScannerOffFragment,
                        ScannerOffFragmentArgs(screen.currentScannerId).toBundle(),
                    )

                    is ConnectScannerIssueScreen.ScannerError -> screen.currentScannerId?.let {
                        showKnownScannerDialog(it)
                    }

                    is ConnectScannerIssueScreen.Ota -> internalNavController?.navigateSafely(
                        currentlyDisplayedInternalFragment,
                        R.id.otaFragment,
                        OtaFragmentArgs(
                            OtaFragmentParams(
                                args.params.fingerprintSDK,
                                screen.availableOtas,
                            ),
                        ).toBundle(),
                    )
                }
            },
        )
        activityViewModel.scannerConnected.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { isSuccess ->
                if (isSuccess) {
                    Vibrate.vibrate(requireContext())
                    activityViewModel.finishConnectionFlow(true)
                }
            },
        )

        activityViewModel.finish.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { isSuccess ->
                finishWithResult(isSuccess)
            },
        )

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activityViewModel.handleBackPress()
        }

        internalNavController?.setGraph(R.navigation.graph_connect_internal)

        if (shouldRequestPermissions) {
            shouldRequestPermissions = false
            checkBluetoothPermissions()
        } else {
            alertHelper.handleResume { shouldRequestPermissions = true }
        }
    }

    private fun showKnownScannerDialog(scannerId: String) {
        if (knownScannedDialog == null) {
            knownScannedDialog = MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(IDR.string.fingerprint_connect_scanner_confirmation_yes) { _, _ ->
                    activityViewModel.handleScannerDisconnectedYesClick()
                }.setNegativeButton(IDR.string.fingerprint_connect_scanner_confirmation_no) { _, _ ->
                    activityViewModel.handleScannerDisconnectedNoClick()
                }.setCancelable(false)
                .create()
        }
        // Update scannerId in case it has changed
        knownScannedDialog?.setTitle(
            getString(
                IDR.string.fingerprint_connect_scanner_id_confirmation_message,
                scannerId,
            ),
        )
        if (internalNavController?.currentDestination?.id == R.id.connectProgressFragment) {
            knownScannedDialog?.takeUnless { it.isShowing }?.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_SHOULD_REQUEST_PERMISSIONS, shouldRequestPermissions)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        if (shouldRequestPermissions) {
            shouldRequestPermissions = false
            checkBluetoothPermissions()
        } else {
            alertHelper.handleResume { shouldRequestPermissions = true }
        }
    }

    override fun onPause() {
        knownScannedDialog?.dismiss()
        super.onPause()
    }

    private fun checkBluetoothPermissions() {
        if (hasBluetoothPermissions()) {
            activityViewModel.connect()
        } else {
            requestBluetoothPermissions()
        }
    }

    private fun hasBluetoothPermissions(): Boolean = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        true
    } else {
        requireActivity().hasPermissions(bluetoothPermissions)
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionsCall.launch(bluetoothPermissions)
        }
    }

    private fun showAlert(error: AlertError) {
        findNavController().navigateSafely(
            this,
            R.id.action_global_to_alertFragment,
            error.toAlertConfig().toArgs(),
        )
    }

    private fun handleResult(result: AlertResult) {
        alertHelper.handleAlertResult(
            requireActivity(),
            result,
            showRefusal = {},
            retry = {},
            finishWithError = { finishWithResult(false) },
        )
    }

    private fun showExitForm() {
        findNavController().navigateSafely(
            this,
            R.id.action_global_to_exitFormFragment,
        )
    }

    private fun handleExitForm(result: ExitFormResult) {
        val option = result.submittedOption()
        if (option != null) {
            findNavController().finishWithResult(this, result)
        } else {
            shouldRequestPermissions = true
        }
    }

    private fun finishWithResult(isSuccess: Boolean) {
        findNavController().finishWithResult(this, FingerprintConnectResult(isSuccess))
    }

    companion object {
        private const val KEY_SHOULD_REQUEST_PERMISSIONS = "KEY_SHOULD_REQUEST_PERMISSIONS"
    }
}
