package com.simprints.fingerprint.connect.screens.issues.bluetoothoff

import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentBluetoothOffBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.fingerprint.infra.scanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.infra.uibase.extensions.showToast
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
class BluetoothOffFragment : Fragment(R.layout.fragment_bluetooth_off) {

    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()

    @Inject
    lateinit var bluetoothAdapter: ComponentBluetoothAdapter

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    private val binding by viewBinding(FragmentBluetoothOffBinding::bind)

    private val bluetoothOnReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ComponentBluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(
                    ComponentBluetoothAdapter.EXTRA_STATE,
                    ComponentBluetoothAdapter.ERROR
                )) {
                    ComponentBluetoothAdapter.STATE_ON -> handleBluetoothEnabled()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportBluetoothNotEnabled()

        binding.turnOnBluetoothButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled()) {
                handleBluetoothEnabled()
            } else {
                tryEnableBluetooth()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().registerReceiver(
            bluetoothOnReceiver,
            IntentFilter(ComponentBluetoothAdapter.ACTION_STATE_CHANGED)
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(bluetoothOnReceiver)
    }

    private val enableBluetoothLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            handleCouldNotEnable()
        }
    }

    private fun tryEnableBluetooth() {
        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun handleCouldNotEnable() {
        requireContext().showToast(IDR.string.bluetooth_off_toast_error)
    }

    private fun handleBluetoothEnabled() {
        binding.apply {
            turnOnBluetoothProgressBar.visibility = View.INVISIBLE
            turnOnBluetoothButton.visibility = View.VISIBLE
            turnOnBluetoothButton.isEnabled = false
            turnOnBluetoothButton.text = getString(IDR.string.bluetooth_on)
            turnOnBluetoothButton.setBackgroundColor(resources.getColor(IDR.color.simprints_green, null))
        }

        lifecycleScope.launch {
            delay(FINISHED_TIME_DELAY_MS)
            retryConnectAndFinishFragment()
        }
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.connect()
        findNavController().navigate(
            BluetoothOffFragmentDirections.actionIssueBluetoothOffFragmentToConnectProgressFragment(),
            navOptions { popUpTo(R.id.connectProgressFragment) }
        )

        findNavController().popBackStack()
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
