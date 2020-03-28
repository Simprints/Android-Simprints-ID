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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.tools.extensions.showToast
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import kotlinx.android.synthetic.main.fragment_bluetooth_off.*
import org.koin.android.ext.android.inject

class BluetoothOffFragment : Fragment() {

    private val bluetoothAdapter: ComponentBluetoothAdapter by inject()

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

        activity?.registerReceiver(bluetoothOnReceiver, IntentFilter(ComponentBluetoothAdapter.ACTION_STATE_CHANGED))

        turnOnBluetoothButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled()) {
                handleBluetoothEnabled()
            } else {
                tryEnableBluetooth()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(bluetoothOnReceiver)
    }

    private fun tryEnableBluetooth() {
        if (bluetoothAdapter.enable()) {
            turnOnBluetoothProgressBar.visibility = View.VISIBLE
            turnOnBluetoothButton.visibility = View.INVISIBLE
            turnOnBluetoothButton.text = ""
            turnOnBluetoothButton.isEnabled = false
        } else {
            handleCouldNotEnable()
        }
    }

    private fun handleCouldNotEnable() {
        context?.showToast("Could not turn on bluetooth. Please turn on bluetooth in device settings.")
    }

    private fun handleBluetoothEnabled() {
        turnOnBluetoothProgressBar.visibility = View.INVISIBLE
        turnOnBluetoothButton.visibility = View.VISIBLE
        turnOnBluetoothButton.isEnabled = false
        turnOnBluetoothButton.setText(R.string.bluetooth_on)
        turnOnBluetoothButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ findNavController().popBackStack() }, FINISHED_TIME_DELAY_MS)
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1200L
    }
}
