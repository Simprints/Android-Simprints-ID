package com.simprints.feature.troubleshooting.recordsmigration

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.feature.troubleshooting.R
import com.simprints.feature.troubleshooting.adapter.TroubleshootingListAdapter
import com.simprints.feature.troubleshooting.databinding.FragmentTroubleshootingListBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class RealmToRoomRecordsMigrationFragment : Fragment(R.layout.fragment_troubleshooting_list) {
    private val viewModel by viewModels<RealmToRoomRecordsMigrationViewModel>()
    private val binding by viewBinding(FragmentTroubleshootingListBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.logs.observe(viewLifecycleOwner) {
            binding.troubleshootingListProgress.isGone = it.isNotEmpty()
            binding.troubleshootingList.adapter = TroubleshootingListAdapter(it) {
                // when more button is clicked delete all realm files
                deleteAllNewDBFiles()
                Toast.makeText(requireContext(), "New DB files deleted", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.collectData()
    }

    // Todo  completely remove this functionality before release
    fun deleteAllNewDBFiles() {
        context?.getDatabasePath("db-subjects")?.delete()
        context?.getDatabasePath("db-subjects.corrupt")?.delete()
    }
}
