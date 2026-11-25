package com.simprints.feature.dashboard.main.dailyactivity

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.databinding.FragmentDashboardCardDailyActivityBinding
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class DailyActivityFragment : Fragment(R.layout.fragment_dashboard_card_daily_activity) {
    private val viewModel by viewModels<DailyActivityViewModel>()
    private val binding by viewBinding(FragmentDashboardCardDailyActivityBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAction(DailyActivityUiAction.Load)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.uiEffect.collect(::handleEffect) }
            }
        }
    }

    // DailyActivityUiEffect has no effects; collected anyway so every screen has wiring
    @Suppress("UNUSED_PARAMETER")
    private fun handleEffect(effect: DailyActivityUiEffect) = Unit

    private fun render(state: DailyActivityUiState) {
        // null: no load finished yet, the card keeps its XML default (gone).
        val activity = state.activity ?: return
        binding.dashboardDailyActivityCard.isVisible = !activity.hasNoActivity
        binding.dashboardDailyActivityCardTitle.text = String.format(
            getString(IDR.string.dashboard_activity_card_activity),
            activity.currentDate,
        )
        renderCount(
            isShown = activity.hasEnrolments,
            count = activity.enrolments,
            group = binding.groupEnrolments,
            countView = binding.dashboardDailyActivityCardEnrolmentsCount,
            labelView = binding.enrolmentsLabel,
            labelRes = IDR.plurals.dashboard_activity_card_enrolments,
        )
        renderCount(
            isShown = activity.hasIdentifications,
            count = activity.identifications,
            group = binding.groupIdentifications,
            countView = binding.dashboardDailyActivityCardIdentificationsCount,
            labelView = binding.identificationsLabel,
            labelRes = IDR.plurals.dashboard_activity_card_identifications,
        )
        renderCount(
            isShown = activity.hasVerifications,
            count = activity.verifications,
            group = binding.groupVerifications,
            countView = binding.dashboardDailyActivityCardVerificationsCount,
            labelView = binding.verificationsLabel,
            labelRes = IDR.plurals.dashboard_activity_card_verifications,
        )
        binding.dividerEnrolments.isVisible = activity.isEnrolmentsDividerVisible
        binding.dividerIdentifications.isVisible = activity.isIdentificationsDividerVisible
    }

    private fun renderCount(
        isShown: Boolean,
        count: Int,
        group: Group,
        countView: TextView,
        labelView: TextView,
        @PluralsRes labelRes: Int,
    ) {
        if (isShown) {
            group.visibility = View.VISIBLE
            countView.text = count.toString()
            labelView.text = resources.getQuantityString(labelRes, count)
        } else {
            group.visibility = View.GONE
        }
    }
}
