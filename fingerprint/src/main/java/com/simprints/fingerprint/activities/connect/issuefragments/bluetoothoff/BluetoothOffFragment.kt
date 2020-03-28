package com.simprints.fingerprint.activities.connect.issuefragments.bluetoothoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simprints.fingerprint.R

class BluetoothOffFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bluetooth_off, container, false)
}
