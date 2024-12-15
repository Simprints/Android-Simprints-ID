package com.simprints.fingerprint.connect.screens.ota.failed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.simprints.core.tools.utils.TimeUtils.getFormattedEstimatedOutage
import com.simprints.fingerprint.connect.R
import com.simprints.fingerprint.connect.databinding.FragmentOtaFailedBinding
import com.simprints.fingerprint.connect.screens.ConnectScannerViewModel
import com.simprints.fingerprint.connect.screens.ota.FetchOtaResult
import com.simprints.fingerprint.connect.usecase.ReportAlertScreenEventUseCase
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

/**
 * This fragment is show when an Over The Air update fails,
 * have a look at the readme for more details - /connect/README.md
 */
@AndroidEntryPoint
internal class OtaFailedFragment : Fragment(R.layout.fragment_ota_failed) {
    private val args by navArgs<OtaFailedFragmentArgs>()
    private val connectScannerViewModel: ConnectScannerViewModel by activityViewModels()
    private val binding by viewBinding(FragmentOtaFailedBinding::bind)

    @Inject
    lateinit var screenReporter: ReportAlertScreenEventUseCase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_ota_failed, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        screenReporter.reportOtaFailed()
        connectScannerViewModel.setBackButtonToExitWithError()

        setInstructions(args.fetchResult)

        binding.continueButton.setOnClickListener {
            connectScannerViewModel.finishConnectionFlow(false)
        }
    }

    private fun setInstructions(fetchOtaResult: FetchOtaResult?) {
        binding.otaFailedInstructionsTextView.text = if (fetchOtaResult?.isMaintenanceMode == true) {
            if (fetchOtaResult.estimatedOutage != null && fetchOtaResult.estimatedOutage != 0L) {
                getString(
                    IDR.string.error_backend_maintenance_with_time_message,
                    getFormattedEstimatedOutage(fetchOtaResult.estimatedOutage),
                )
            } else {
                getString(IDR.string.error_backend_maintenance_message)
            }
        } else {
            getString(IDR.string.fingerprint_connect_ota_failed_instructions)
        }
    }
}
