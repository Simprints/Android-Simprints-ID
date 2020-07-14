package com.simprints.fingerprint.activities.connect.issues.bluetoothoff

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.ConnectScannerViewModel
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import kotlinx.android.synthetic.main.fragment_bluetooth_off.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class BluetoothOffFragment : FingerprintFragment() {

    private val connectScannerViewModel: ConnectScannerViewModel by sharedViewModel()
    private val bluetoothAdapter: ComponentBluetoothAdapter by inject()
    private val sessionManager: FingerprintSessionEventsManager by inject()
    private val timeHelper: FingerprintTimeHelper by inject()

    private val bluetoothOnReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ComponentBluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(ComponentBluetoothAdapter.EXTRA_STATE, ComponentBluetoothAdapter.ERROR)) {
                    ComponentBluetoothAdapter.STATE_ON -> handleBluetoothEnabled()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bluetooth_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.BluetoothOff))

        turnOnBluetoothButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled()) {
                handleBluetoothEnabled()
            } else {
                tryEnableBluetooth()
            }
        }
    }

    private fun setTextInLayout() {
        turnOnBluetoothButton.text = getString(R.string.turn_on_bluetooth)
        bluetoothOffTitleTextView.text = getString(R.string.bluetooth_off_title)
    }

    override fun onStart() {
        super.onStart()
        activity?.registerReceiver(bluetoothOnReceiver, IntentFilter(ComponentBluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(bluetoothOnReceiver)
    }

    private fun tryEnableBluetooth() {
        if (bluetoothAdapter.enable()) {
            turnOnBluetoothProgressBar.visibility = View.VISIBLE
            turnOnBluetoothButton.visibility = View.INVISIBLE
            turnOnBluetoothButton.isEnabled = false
        } else {
            handleCouldNotEnable()
        }
    }

    private fun handleCouldNotEnable() {
        context?.showToast(getString(R.string.bluetooth_off_toast_error))
    }

    private fun handleBluetoothEnabled() {
        turnOnBluetoothProgressBar.visibility = View.INVISIBLE
        turnOnBluetoothButton.visibility = View.VISIBLE
        turnOnBluetoothButton.isEnabled = false
        turnOnBluetoothButton.text = getString(R.string.bluetooth_on)
        turnOnBluetoothButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ retryConnectAndFinishFragment() }, FINISHED_TIME_DELAY_MS)
    }

    private fun retryConnectAndFinishFragment() {
        connectScannerViewModel.retryConnect()
        findNavController().navigate(R.id.action_bluetoothOffFragment_to_connectScannerMainFragment)
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
