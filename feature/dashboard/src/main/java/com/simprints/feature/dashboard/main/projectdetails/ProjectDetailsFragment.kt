package com.simprints.feature.dashboard.main.projectdetails

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardProjectDetailsBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ProjectDetailsFragment : Fragment(R.layout.fragment_dashboard_card_project_details) {
    private val viewModel by viewModels<ProjectDetailsViewModel>()
    private val binding by viewBinding(FragmentDashboardCardProjectDetailsBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAction(ProjectDetailsUiAction.Load)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    // ProjectDetailsUiEffect has no effects; collected anyway so every screen has wiring
    @Suppress("UNUSED_PARAMETER")
    private fun handleEffect(effect: ProjectDetailsUiEffect) = Unit

    private fun render(state: ProjectDetailsUiState) {
        // null: not loaded yet, the card keeps its XML default (visible, empty).
        val isLoaded = state.isLoaded ?: return
        binding.dashboardProjectDetailsCard.isVisible = isLoaded
        binding.dashboardProjectDetailsCardTitle.text = state.title
        binding.dashboardProjectDetailsCardCurrentUser.text = String.format(
            getString(IDR.string.dashboard_project_card_current_user),
            state.lastUser,
        )
        with(binding.dashboardProjectDetailsCardScannerUsed) {
            if (state.lastScanner.isEmpty()) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                text = String.format(
                    context.getString(IDR.string.dashboard_project_card_scanner_used),
                    state.lastScanner,
                )
            }
        }
    }
}
