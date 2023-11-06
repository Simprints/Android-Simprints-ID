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
import com.simprints.feature.exitform.exitFormConfiguration
import com.simprints.feature.exitform.scannerOptions
import com.simprints.feature.exitform.toArgs
import com.simprints.fingerprint.connect.FingerprintConnectResult
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel.ConnectScannerIssueScreen
import com.simprints.fingerprint.connect.screens.alert.AlertActivityHelper
import com.simprints.fingerprint.connect.screens.alert.AlertError
import com.simprints.fingerprint.connect.screens.issues.scanneroff.ScannerOffFragmentArgs
import com.simprints.fingerprint.connect.screens.ota.OtaFragmentArgs
import com.simprints.fingerprint.connect.screens.ota.OtaFragmentParams
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.system.Vibrate
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ConnectScannerControllerFragment : Fragment(R.layout.fragment_connect_scanner) {

    private var shouldRequestPermissions = true

    private val args: ConnectScannerControllerFragmentArgs by navArgs()
    private val viewModel: ConnectScannerViewModel by activityViewModels()

    private val alertHelper = AlertActivityHelper()

    private var knownScannedDialog: AlertDialog? = null

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    @RequiresApi(Build.VERSION_CODES.S)
    private val bluetoothPermissionsCall = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        val permission = permissions
            .filterKeys(bluetoothPermissions::contains)
            .map { (permission, isGranted) -> requireActivity().permissionFromResult(permission, isGranted) }
            .worstPermissionStatus()

        Simber.i("Bluetooth permission: $permission")

        when (permission) {
            PermissionStatus.Granted -> viewModel.connect()
            PermissionStatus.Denied -> requestBluetoothPermissions()
            PermissionStatus.DeniedNeverAskAgain -> viewModel.handleNoBluetoothPermission()
        }
    }

    private fun internalNavController() = childFragmentManager
        .findFragmentById(R.id.connect_scanner_host_fragment)
        ?.findNavController()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.init(args.params)

        findNavController().handleResult(this, R.id.connectScannerControllerFragment, ExitFormContract.DESTINATION, ::handleExitForm)
        findNavController().handleResult(this, R.id.connectScannerControllerFragment, AlertContract.DESTINATION, ::handleResult)

        viewModel.showScannerIssueScreen.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { screen ->
            when (screen) {
                ConnectScannerIssueScreen.BluetoothNoPermission -> showAlert(AlertError.BLUETOOTH_NO_PERMISSION)
                ConnectScannerIssueScreen.BluetoothNotSupported -> showAlert(AlertError.BLUETOOTH_NOT_SUPPORTED)
                ConnectScannerIssueScreen.LowBattery -> showAlert(AlertError.LOW_BATTERY)
                ConnectScannerIssueScreen.UnexpectedError -> showAlert(AlertError.UNEXPECTED_ERROR)

                ConnectScannerIssueScreen.ExitForm -> showExitForm()

                ConnectScannerIssueScreen.BluetoothOff -> internalNavController()?.navigate(R.id.issueBluetoothOffFragment)
                ConnectScannerIssueScreen.NfcOff -> internalNavController()?.navigate(R.id.issueNfcOffFragment)
                ConnectScannerIssueScreen.NfcPair -> internalNavController()?.navigate(R.id.issueNfcPairFragment)
                ConnectScannerIssueScreen.SerialEntryPair -> internalNavController()?.navigate(R.id.issueSerialEntryPairFragment)

                is ConnectScannerIssueScreen.ScannerOff -> internalNavController()?.navigate(
                    R.id.issueScannerOffFragment,
                    ScannerOffFragmentArgs(screen.currentScannerId).toBundle()
                )

                is ConnectScannerIssueScreen.ScannerError -> screen.currentScannerId?.let { showKnownScannerDialog(it) }

                is ConnectScannerIssueScreen.Ota -> internalNavController()?.navigate(
                    R.id.otaFragment,
                    OtaFragmentArgs(OtaFragmentParams(screen.availableOtas)).toBundle()
                )
            }
        })
        viewModel.scannerConnected.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { isSuccess ->
            if (isSuccess) {
                Vibrate.vibrate(requireContext())
                viewModel.finishConnectionFlow(true)
            }
        })

        viewModel.finish.observe(viewLifecycleOwner, LiveDataEventWithContentObserver { isSuccess ->
            finishWithResult(isSuccess)
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.handleBackPress()
        }

        internalNavController()?.setGraph(R.navigation.graph_connect_internal)

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
                .setTitle(getString(IDR.string.scanner_id_confirmation_message, scannerId))
                .setPositiveButton(IDR.string.scanner_confirmation_yes) { _, _ ->
                    viewModel.handleScannerDisconnectedYesClick()
                }
                .setNegativeButton(IDR.string.scanner_confirmation_no) { _, _ ->
                    viewModel.handleScannerDisconnectedNoClick()
                }
                .setCancelable(false)
                .create()
        }
        knownScannedDialog?.takeUnless { it.isShowing }?.show()
    }

    override fun onResume() {
        super.onResume()
        alertHelper.handleResume { shouldRequestPermissions = true }
    }

    override fun onPause() {
        knownScannedDialog?.dismiss()
        super.onPause()
    }

    private fun checkBluetoothPermissions() {
        if (hasBluetoothPermissions()) viewModel.connect()
        else requestBluetoothPermissions()
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) true
        else requireActivity().hasPermissions(bluetoothPermissions)
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionsCall.launch(bluetoothPermissions)
        }
    }

    private fun showAlert(error: AlertError) {
        findNavController().navigate(R.id.action_global_to_alertFragment, error.toAlertConfig().toArgs())
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
        findNavController().navigate(
            R.id.action_global_to_exitFormFragment,
            exitFormConfiguration {
                titleRes = IDR.string.why_did_you_skip_fingerprinting
                backButtonRes = IDR.string.button_scan_prints
                visibleOptions = scannerOptions()
            }.toArgs()
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

}
