package com.simprints.fingerprint.activities.connect.issues.nfcoff

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import com.simprints.fingerprint.tools.nfc.ComponentNfcAdapter
import kotlinx.android.synthetic.main.fragment_nfc_off.*
import org.koin.android.ext.android.inject

class NfcOffFragment : Fragment() {

    private var handlingNfcEnabled = false

    private val nfcAdapter: ComponentNfcAdapter by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nfc_off, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (nfcAdapter.isNull()) {
            continueToSerialEntryPair()
            return
        }

        turnOnNfcButton.setOnClickListener {
            val enableNfcIntent = Intent(Settings.ACTION_NFC_SETTINGS)
            startActivityForResult(enableNfcIntent, REQUEST_ENABLE_NFC)
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter.isEnabled()) handleNfcEnabled()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_NFC -> {
                if (nfcAdapter.isEnabled()) {
                    handleNfcEnabled()
                }
            }
        }
    }

    private fun handleNfcEnabled() {
        if (handlingNfcEnabled) return
        handlingNfcEnabled = true
        turnOnNfcButton.isEnabled = false
        turnOnNfcButton.setText(R.string.nfc_on)
        turnOnNfcButton.setBackgroundColor(resources.getColor(R.color.simprints_green, null))
        Handler().postDelayed({ continueToNfcPair() }, FINISHED_TIME_DELAY_MS)
    }

    private fun continueToNfcPair() {
        findNavController().navigate(R.id.action_nfcOffFragment_to_nfcPairFragment)
    }

    private fun continueToSerialEntryPair() {
        findNavController().navigate(R.id.action_nfcOffFragment_to_serialEntryFragment)
    }

    companion object {
        private const val REQUEST_ENABLE_NFC = 10
        private const val FINISHED_TIME_DELAY_MS = 2000L
    }
}
