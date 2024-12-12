package com.simprints.fingerprint.connect.screens.issues.nfcoff

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentNfcOffBinding
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.fingerprint.infra.scanner.NfcManager
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class NfcOffFragment : Fragment(R.layout.fragment_nfc_off) {
    private var handlingNfcEnabled = false

    private val binding by viewBinding(FragmentNfcOffBinding::bind)

    @Inject
    lateinit var nfcManager: NfcManager

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    private val nfcEnabler = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (nfcManager.isNfcEnabled()) {
            handleNfcEnabled()
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportNfcNotEnabled()

        if (!nfcManager.doesDeviceHaveNfcCapability()) {
            continueToSerialEntryPair()
            return
        }

        binding.turnOnNfcButton.setOnClickListener {
            nfcEnabler.launch(Intent(Settings.ACTION_NFC_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        if (nfcManager.isNfcEnabled()) handleNfcEnabled()
    }

    private fun handleNfcEnabled() {
        if (handlingNfcEnabled) return
        handlingNfcEnabled = true
        binding.turnOnNfcButton.isEnabled = false
        binding.turnOnNfcButton.text = getString(IDR.string.fingerprint_connect_nfc_on)
        binding.turnOnNfcButton.setBackgroundColor(
            resources.getColor(
                IDR.color.simprints_green,
                null,
            ),
        )
        lifecycleScope.launch {
            delay(FINISHED_TIME_DELAY_MS)
            continueToNfcPair()
        }
    }

    private fun continueToNfcPair() {
        findNavController().navigateSafely(this, NfcOffFragmentDirections.actionNfcOffFragmentToNfcPairFragment())
    }

    private fun continueToSerialEntryPair() {
        findNavController().navigateSafely(this, NfcOffFragmentDirections.actionNfcOffFragmentToSerialEntryPairFragment())
    }

    companion object {
        private const val FINISHED_TIME_DELAY_MS = 1500L
    }
}
