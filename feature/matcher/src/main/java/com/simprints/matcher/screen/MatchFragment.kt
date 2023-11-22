package com.simprints.matcher.screen

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.matcher.R
import com.simprints.matcher.databinding.FragmentMatcherBinding
import com.simprints.matcher.screen.MatchViewModel.MatchState.Finished
import com.simprints.matcher.screen.MatchViewModel.MatchState.LoadingCandidates
import com.simprints.matcher.screen.MatchViewModel.MatchState.Matching
import com.simprints.matcher.screen.MatchViewModel.MatchState.NotStarted
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class MatchFragment : Fragment(R.layout.fragment_matcher) {

    private val viewModel: MatchViewModel by viewModels()
    private val binding by viewBinding(FragmentMatcherBinding::bind)
    private val args by navArgs<MatchFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.setupMatch(args.params)
    }

    private fun setIdentificationProgress(progress: Int) = requireActivity().runOnUiThread {
        ObjectAnimator
            .ofInt(binding.faceMatchProgress, "progress", binding.faceMatchProgress.progress, progress)
            .setDuration(progress * PROGRESS_DURATION_MULTIPLIER)
            .start()
    }

    private fun observeViewModel() {
        viewModel.matchResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            // wait a bit for the user to see the results
            Handler(Looper.getMainLooper()).postDelayed(
                { findNavController().finishWithResult(this, it) },
                MatchViewModel.matchingEndWaitTimeInMillis
            )
        })
        viewModel.matchState.observe(viewLifecycleOwner) { matchState ->
            when (matchState) {
                NotStarted -> renderNotStarted()
                LoadingCandidates -> renderLoadingCandidates()
                is Matching -> renderMatching()
                is Finished -> renderFinished(matchState)
            }
        }
    }

    private fun renderNotStarted() = binding.apply {
        faceMatchTvMatchingProgressStatus1.isGone = true
        faceMatchTvMatchingProgressStatus2.isGone = true
        faceMatchProgress.isGone = true
        faceMatchTvMatchingResultStatus1.isGone = true
        faceMatchTvMatchingResultStatus2.isGone = true
        faceMatchTvMatchingResultStatus3.isGone = true
    }

    private fun renderLoadingCandidates() {
        binding.apply {
            faceMatchTvMatchingProgressStatus1.isVisible = true
            faceMatchTvMatchingProgressStatus1.text = getString(IDR.string.matcher_loading_candidates)
            faceMatchProgress.isVisible = true
        }
        setIdentificationProgress(LOADING_PROGRESS)
    }

    private fun renderMatching() {
        binding.faceMatchTvMatchingProgressStatus1.text = getString(IDR.string.matcher_matching_candidates)

        setIdentificationProgress(MATCHING_PROGRESS)
    }

    private fun renderFinished(matchState: Finished) {
        binding.faceMatchTvMatchingProgressStatus1.text = resources.getQuantityString(
            IDR.plurals.matcher_matched_candidates,
            matchState.candidatesMatched,
            matchState.candidatesMatched
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = resources.getQuantityString(
            IDR.plurals.matcher_returned_results,
            matchState.returnSize,
            matchState.returnSize
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = resources.getQuantityString(
                IDR.plurals.matcher_tier1or2_matches,
                matchState.veryGoodMatches,
                matchState.veryGoodMatches
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = resources.getQuantityString(
                IDR.plurals.matcher_tier3_matches,
                matchState.goodMatches,
                matchState.goodMatches
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = resources.getQuantityString(
                IDR.plurals.matcher_tier4_matches,
                matchState.fairMatches,
                matchState.fairMatches
            )
        }

        setIdentificationProgress(MAX_PROGRESS)
    }

    companion object {
        private const val MAX_PROGRESS = 100
        private const val PROGRESS_DURATION_MULTIPLIER = 10L
        private const val LOADING_PROGRESS = 25
        private const val MATCHING_PROGRESS = 50
    }
}
