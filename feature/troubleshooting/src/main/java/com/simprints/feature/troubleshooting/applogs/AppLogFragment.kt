package com.simprints.feature.troubleshooting.applogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.feature.troubleshooting.databinding.FragmentAppLogsBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class AppLogFragment : Fragment(R.layout.fragment_app_logs) {
    private val viewModel by viewModels<AppLogViewModel>()
    private val binding by viewBinding(FragmentAppLogsBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val headerAdapter = AppLogHeaderAdapter(
            onQueryChanged = viewModel::setQuery,
            onGroupsChanged = viewModel::setGroups,
        )
        val logAdapter = TroubleshootingListAdapter(emptyList()) { showFullEntry(it) }
        binding.appLogsList.adapter = ConcatAdapter(headerAdapter, logAdapter)

        viewModel.deviceInfo.observe(viewLifecycleOwner) { headerAdapter.setDeviceInfo(it) }
        viewModel.summary.observe(viewLifecycleOwner) { headerAdapter.setSummary(it) }
        viewModel.logs.observe(viewLifecycleOwner) { logAdapter.submit(it) }
        viewModel.collectData()
    }

    private fun showFullEntry(navigationId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(viewModel.fullTextFor(navigationId))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
