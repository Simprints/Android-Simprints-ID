package com.simprints.fingerprint.activities.connect.issues.nfcoff

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.connect.issues.ConnectScannerIssue
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.model.AlertScreenEventWithScannerIssue
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.fingerprint.NfcManager
import com.simprints.fingerprint.databinding.FragmentNfcOffBinding
import org.koin.android.ext.android.inject

class NfcOffFragment : FingerprintFragment() {

    private var handlingNfcEnabled = false

    private val nfcManager: NfcManager by inject()
    private val binding by viewBinding(FragmentNfcOffBinding::bind)
    private val sessionManager: FingerprintSessionEventsManager by inject()
    private val timeHelper: FingerprintTimeHelper by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nfc_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextInLayout()

        sessionManager.addEventInBackground(AlertScreenEventWithScannerIssue(timeHelper.now(), ConnectScannerIssue.NfcOff))

        if (!nfcManager.doesDeviceHaveNfcCapability()) {
            continueToSerialEntryPair()
            return
        }

        binding.turnOnNfcButton.setOnClickListener {
            val enableNfcIntent = Intent(Settings.ACTION_NFC_SETTINGS)
            startActivityForResult(enableNfcIntent, REQUEST_ENABLE_NFC)
        }
    }

    private fun setTextInLayout() {
        binding.turnOnNfcButton.text = getString(R.string.turn_on_nfc)
        binding.nfcOffTitleTextView.text = getString(R.string.nfc_off_title)
    }

    override fun onResume() {
        super.onResume()
        if (nfcManager.isNfcEnabled()) handleNfcEnabled()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_NFC -> {
                if (nfcManager.isNfcEnabled()) {
                    handleNfcEnabled()
                }
            }
        }
    }

    private fun handleNfcEnabled() {
        if (handlingNfcEnabled) return
        handlingNfcEnabled = true
        binding.turnOnNfcButton.isEnabled = false
        binding.turnOnNfcButton.text = getString(R.string.nfc_on)
        binding.turnOnNfcButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ continueToNfcPair() }, FINISHED_TIME_DELAY_MS)
    }

    private fun continueToNfcPair() {
        findNavController().navigate(R.id.action_nfcOffFragment_to_nfcPairFragment)
    }

    private fun continueToSerialEntryPair() {
        findNavController().navigate(R.id.action_nfcOffFragment_to_serialEntryPairFragment)
    }

    companion object {
        private const val REQUEST_ENABLE_NFC = 10
        private const val FINISHED_TIME_DELAY_MS = 1500L
    }
}
