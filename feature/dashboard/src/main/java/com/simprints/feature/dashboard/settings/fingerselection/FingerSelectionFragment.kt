package com.simprints.feature.dashboard.settings.fingerselection

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentFingerSelectionBinding
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        applySystemBarInsets(view)

        initRecyclerView()
        observeState()
        viewModel.onAction(FingerSelectionUiAction.Load)
        binding.settingsToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initRecyclerView() {
        fingerSelectionAdapter = FingerSelectionItemAdapter()
        binding.fingerSelectionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.fingerSelectionRecyclerView.adapter = fingerSelectionAdapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    // FingerSelectionUiEffect has no effects; collected anyway so every screen has wiring
    @Suppress("UNUSED_PARAMETER")
    private fun handleEffect(effect: FingerSelectionUiEffect) = Unit

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: FingerSelectionUiState) {
        // null: not loaded yet, the list stays empty.
        val sections = state.sections ?: return
        fingerSelectionAdapter.submit(sections)
        fingerSelectionAdapter.notifyDataSetChanged()
    }
}
