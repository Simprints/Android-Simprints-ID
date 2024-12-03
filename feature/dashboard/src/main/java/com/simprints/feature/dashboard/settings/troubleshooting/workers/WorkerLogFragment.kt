package com.simprints.feature.dashboard.settings.troubleshooting.workers

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentTroubleshootingListBinding
import com.simprints.feature.dashboard.settings.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class WorkerLogFragment  : Fragment(R.layout.fragment_troubleshooting_list) {

    private val viewModel by viewModels<WorkerLogViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingListBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.workers.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter = TroubleshootingListAdapter(it)
        }
        viewModel.collectWorkerData()
    }
}
