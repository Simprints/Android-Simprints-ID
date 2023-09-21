package com.simprints.face.matcher.screen

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
import com.simprints.face.matcher.R
import com.simprints.face.matcher.databinding.FragmentMatcherBinding
import com.simprints.face.matcher.screen.FaceMatchViewModel.MatchState.Finished
import com.simprints.face.matcher.screen.FaceMatchViewModel.MatchState.LoadingCandidates
import com.simprints.face.matcher.screen.FaceMatchViewModel.MatchState.Matching
import com.simprints.face.matcher.screen.FaceMatchViewModel.MatchState.NotStarted
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class FaceMatchFragment : Fragment(R.layout.fragment_matcher) {

    private val viewModel: FaceMatchViewModel by viewModels()
    private val binding by viewBinding(FragmentMatcherBinding::bind)
    private val args by navArgs<FaceMatchFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        viewModel.setupMatch(args.params)
    }

    private fun setIdentificationProgress(progress: Int) = requireActivity().runOnUiThread {
        ObjectAnimator
            .ofInt(binding.faceMatchProgress, "progress", binding.faceMatchProgress.progress, progress)
            .setDuration((progress * 10).toLong())
            .start()
    }

    private fun observeViewModel() {
        viewModel.faceMatchResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            // wait a bit for the user to see the results
            Handler(Looper.getMainLooper()).postDelayed(
                { findNavController().finishWithResult(this, it) },
                FaceMatchViewModel.matchingEndWaitTimeInMillis
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
            faceMatchTvMatchingProgressStatus1.text = getString(IDR.string.face_match_loading_candidates)
            faceMatchProgress.isVisible = true
        }
        setIdentificationProgress(25)
    }

    private fun renderMatching() {
        binding.faceMatchTvMatchingProgressStatus1.text = getString(IDR.string.face_match_matching_candidates)

        setIdentificationProgress(50)
    }

    private fun renderFinished(matchState: Finished) {
        binding.faceMatchTvMatchingProgressStatus1.text = resources.getQuantityString(
            IDR.plurals.face_match_matched_candidates,
            matchState.candidatesMatched,
            matchState.candidatesMatched
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = resources.getQuantityString(
            IDR.plurals.face_match_returned_results,
            matchState.returnSize,
            matchState.returnSize
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = resources.getQuantityString(
                IDR.plurals.face_match_tier1or2_matches,
                matchState.veryGoodMatches,
                matchState.veryGoodMatches
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = resources.getQuantityString(
                IDR.plurals.face_match_tier3_matches,
                matchState.goodMatches,
                matchState.goodMatches
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = resources.getQuantityString(
                IDR.plurals.face_match_tier4_matches,
                matchState.fairMatches,
                matchState.fairMatches
            )
        }

        setIdentificationProgress(100)
    }
}
