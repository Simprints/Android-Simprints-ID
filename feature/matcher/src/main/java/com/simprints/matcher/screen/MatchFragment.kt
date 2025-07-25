package com.simprints.matcher.screen

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.applicationSettingsIntent
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.matcher.MatchParams
import com.simprints.matcher.R
import com.simprints.matcher.databinding.FragmentMatcherBinding
import com.simprints.matcher.screen.MatchViewModel.MatchState
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class MatchFragment : Fragment(R.layout.fragment_matcher) {
    private val viewModel: MatchViewModel by viewModels()
    private val binding by viewBinding(FragmentMatcherBinding::bind)
    private val params: MatchParams by navigationParams()

    private val permissionCall = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val status = params.biometricDataSource
            .permissionName()
            ?.let { requireActivity().permissionFromResult(it, granted) }
            ?: PermissionStatus.Granted

        when (status) {
            PermissionStatus.Granted -> viewModel.setupMatch(params)
            PermissionStatus.Denied -> viewModel.noPermission(neverAskAgain = false)
            PermissionStatus.DeniedNeverAskAgain -> viewModel.noPermission(neverAskAgain = true)
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("MatchFragment started (isFace=${params.isFaceMatch()})", tag = ORCHESTRATION)

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        val requiredPermissionName = params.biometricDataSource
            .permissionName()
            ?.takeUnless { requireActivity().hasPermission(it) }

        // This flag prevents infinite loop of permission checks
        if (viewModel.shouldCheckPermission) {
            viewModel.shouldCheckPermission = false

            if (requiredPermissionName != null) {
                permissionCall.launch(requiredPermissionName)
            } else {
                viewModel.setupMatch(params)
            }
        } else {
            viewModel.shouldCheckPermission = true
        }
    }

    private fun setIdentificationProgress(
        progress: Int,
        animate: Boolean,
    ) = requireActivity().runOnUiThread {
        if (animate) {
            ObjectAnimator
                .ofInt(binding.faceMatchProgress, "progress", binding.faceMatchProgress.progress, progress)
                .setDuration(progress * PROGRESS_DURATION_MULTIPLIER)
                .start()
        } else {
            binding.faceMatchProgress.progress = progress
        }
    }

    private fun observeViewModel() {
        viewModel.matchResponse.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver {
                findNavController().finishWithResult(this, it)
            },
        )
        viewModel.matchState.observe(viewLifecycleOwner) { matchState ->
            when (matchState) {
                MatchState.NotStarted -> renderNotStarted()
                is MatchState.LoadingCandidates -> renderLoadingCandidates(matchState)
                is MatchState.Matching -> renderMatching()
                is MatchState.Finished -> renderFinished(matchState)
                is MatchState.NoPermission -> renderNoPermission(matchState)
            }
        }
    }

    private fun renderNotStarted() = binding.apply {
        binding.faceMatchPermissionRequestButton.isGone = true
        faceMatchTvMatchingProgressStatus1.isGone = true
        faceMatchTvMatchingProgressStatus2.isGone = true
        faceMatchProgress.isGone = true
        faceMatchTvMatchingResultStatus1.isGone = true
        faceMatchTvMatchingResultStatus2.isGone = true
        faceMatchTvMatchingResultStatus3.isGone = true
    }

    private fun renderLoadingCandidates(state: MatchState.LoadingCandidates) {
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.apply {
            val text = if (state.total > 0) {
                getString(IDR.string.matcher_loading_candidates) + " (${state.loaded}/${state.total})"
            } else {
                getString(IDR.string.matcher_loading_candidates)
            }
            faceMatchTvMatchingProgressStatus1.isVisible = true
            faceMatchTvMatchingProgressStatus1.text = text
            faceMatchProgress.isVisible = true
        }

        val progress = calculateLoadingCandidatesProgress(state)
        setIdentificationProgress(progress, animate = false)
    }

    /**
     * Returns value for the progress indicator depending on the current match state. It divides the loading indicator starting from
     * [MATCHING_PROGRESS] up until [LOADING_PROGRESS] into even slices with size of [MatchState.LoadingCandidates.total]. Then it
     * calculates the percentage to display on the progress bar
     *
     * @return integer value between [LOADING_PROGRESS] and [MATCHING_PROGRESS]
     */
    private fun calculateLoadingCandidatesProgress(state: MatchState.LoadingCandidates): Int {
        if (state.total <= 0) return LOADING_PROGRESS
        val diff = MATCHING_PROGRESS - LOADING_PROGRESS // progress diff from the next step
        val slices: Float = diff.toFloat() / state.total
        return LOADING_PROGRESS + (state.loaded * slices).toInt()
    }

    private fun renderMatching() {
        binding.faceMatchTvMatchingProgressStatus1.text =
            getString(IDR.string.matcher_matching_candidates)
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.faceMatchTvMatchingProgressStatus1.setText(IDR.string.matcher_matching_candidates)

        setIdentificationProgress(MATCHING_PROGRESS, animate = true)
    }

    private fun renderFinished(matchState: MatchState.Finished) {
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.faceMatchTvMatchingProgressStatus1.text = resources.getQuantityString(
            IDR.plurals.matcher_matched_candidates,
            matchState.candidatesMatched,
            matchState.candidatesMatched,
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = resources.getQuantityString(
            IDR.plurals.matcher_returned_results,
            matchState.returnSize,
            matchState.returnSize,
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = resources.getQuantityString(
                IDR.plurals.matcher_tier1or2_matches,
                matchState.veryGoodMatches,
                matchState.veryGoodMatches,
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = resources.getQuantityString(
                IDR.plurals.matcher_tier3_matches,
                matchState.goodMatches,
                matchState.goodMatches,
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = resources.getQuantityString(
                IDR.plurals.matcher_tier4_matches,
                matchState.fairMatches,
                matchState.fairMatches,
            )
        }

        setIdentificationProgress(MAX_PROGRESS, animate = true)
    }

    private fun renderNoPermission(state: MatchState.NoPermission) = with(binding) {
        faceMatchMessage.setText(IDR.string.matcher_missing_access_permission)

        val name = params.biometricDataSource.permissionName()
        faceMatchPermissionRequestButton.isVisible = name != null

        faceMatchPermissionRequestButton.setOnClickListener {
            if (state.shouldOpenSettings) {
                startActivity(requireContext().applicationSettingsIntent)
            } else {
                permissionCall.launch(name)
            }
        }
    }

    companion object {
        private const val MAX_PROGRESS = 100
        private const val PROGRESS_DURATION_MULTIPLIER = 10L
        private const val LOADING_PROGRESS = 25
        private const val MATCHING_PROGRESS = 75
    }
}
