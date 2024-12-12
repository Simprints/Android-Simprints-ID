package com.simprints.feature.dashboard.settings.fingerselection

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentFingerSelectionBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class FingerSelectionFragment : Fragment(R.layout.fragment_finger_selection) {
    private val viewModel: FingerSelectionViewModel by viewModels()
    private val binding by viewBinding(FragmentFingerSelectionBinding::bind)

    private lateinit var fingerSelectionAdapter: FingerSelectionItemAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        listenForItemChanges()
        viewModel.start()
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter {
            viewModel.fingerSelections.value ?: emptyList()
        }
        binding.fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenForItemChanges() {
        viewModel.fingerSelections.observe(viewLifecycleOwner) {
            fingerSelectionAdapter.notifyDataSetChanged()
        }
    }
}
