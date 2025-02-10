package com.simprints.feature.troubleshooting.overview

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingOverviewBinding
import com.simprints.feature.troubleshooting.overview.usecase.ExportLogsUseCase.LogsExportResult
import com.simprints.feature.troubleshooting.overview.usecase.PingServerUseCase.PingResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
internal class OverviewFragment : Fragment(R.layout.fragment_troubleshooting_overview) {
    private val viewModel by viewModels<OverviewViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingOverviewBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.projectIds.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewIds.text = it.orEmpty()
        }
        viewModel.configurationDetails.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewConfiguration.text = it.orEmpty()
        }
        viewModel.licenseStates.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewLicences.text = it.ifBlank { "No licenses found" }
        }
        viewModel.networkStates.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewNetwork.text = it.orEmpty()
        }
        viewModel.scannerState.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewScanner.text = it.orEmpty()
        }

        viewModel.collectData()

        viewModel.pingResult.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewPingResult.text = when (it) {
                PingResult.NotDone -> "Check not done yet"
                PingResult.InProgress -> "Calling server"
                is PingResult.Success -> "Success in ${it.message}"
                is PingResult.Failure -> "Failed: ${it.message}"
            }
            binding.troubleshootOverviewPing.isEnabled = it != PingResult.InProgress
        }
        viewModel.logsExportResult.observe(viewLifecycleOwner) {
            binding.troubleshootOverviewExportLogs.isEnabled = it !is LogsExportResult.InProgress
            binding.troubleshootOverviewExportLogsResult.text = when (it) {
                LogsExportResult.InProgress -> "Preparing logs..."
                LogsExportResult.Failed -> "Nothing to export"
                else -> ""
            }

            if (it is LogsExportResult.Success) {
                launchFileExportIntent(it.deviceId, it.file)
            }
        }

        binding.troubleshootOverviewPing.setOnClickListener {
            viewModel.pingServer()
        }
        binding.troubleshootOverviewExportLogs.setOnClickListener { v ->
            viewModel.exportLogs()
        }
    }

    private fun launchFileExportIntent(
        deviceId: String,
        file: File,
    ) {
        val fileProvider = FileProvider.getUriForFile(requireContext(), "com.simprints.id", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(LOG_EMAIL_RECIPIENT))
            putExtra(Intent.EXTRA_SUBJECT, "Reporting logs from $deviceId")
            putExtra(Intent.EXTRA_STREAM, fileProvider)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        requireActivity().startActivity(Intent.createChooser(intent, "Share with"))
    }

    companion object {
        private const val LOG_EMAIL_RECIPIENT = "sid-error-logs@simprints.atlassian.net"
    }
}
