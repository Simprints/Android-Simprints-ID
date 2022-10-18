package com.simprints.face.match

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.face.R
import com.simprints.face.base.FaceActivity
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.databinding.ActivityFaceMatchBinding
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.face.match.FaceMatchViewModel.MatchState.*
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FaceMatchActivity : FaceActivity() {
    private val vm: FaceMatchViewModel by viewModels()
    private val binding by viewBinding(ActivityFaceMatchBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setTextInLayout()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val faceRequest: FaceMatchRequest =
            this.intent.extras?.getParcelable(IFaceRequest.BUNDLE_KEY)
                ?: throw InvalidFaceRequestException("No IFaceRequest found for FaceMatchActivity")

        observeViewModel()
        vm.setupMatch(faceRequest)
    }

    private fun setTextInLayout() {
        binding.faceMatchPleaseWait.text =
            getString(R.string.face_match_please_wait)
    }

    private fun setIdentificationProgress(progress: Int) =
        runOnUiThread {
            ObjectAnimator
                .ofInt(
                    binding.faceMatchProgress,
                    "progress",
                    binding.faceMatchProgress.progress,
                    progress
                )
                .setDuration((progress * 10).toLong())
                .start()
        }

    private fun observeViewModel() {
        vm.faceMatchResponse.observe(this, LiveDataEventWithContentObserver {
            val intent = Intent().apply { putExtra(IFaceResponse.BUNDLE_KEY, it) }
            setResult(Activity.RESULT_OK, intent)
            // wait a bit for the user to see the results
            Handler(Looper.getMainLooper()).postDelayed(
                { finish() },
                FaceMatchViewModel.matchingEndWaitTimeInMillis
            )
        })
        vm.matchState.observe(this) { matchState ->
            when (matchState) {
                NotStarted -> renderNotStarted()
                Error -> TODO()
                LoadingCandidates -> renderLoadingCandidates()
                is Matching -> renderMatching()
                is Finished -> renderFinished(matchState)
            }
        }
    }

    private fun renderNotStarted() {
        binding.apply {
            faceMatchTvMatchingProgressStatus1.isGone = true
            faceMatchTvMatchingProgressStatus2.isGone = true
            faceMatchProgress.isGone = true
            faceMatchTvMatchingResultStatus1.isGone = true
            faceMatchTvMatchingResultStatus2.isGone = true
            faceMatchTvMatchingResultStatus3.isGone = true
        }
    }

    private fun renderLoadingCandidates() {
        binding.apply {
            faceMatchTvMatchingProgressStatus1.isVisible = true
            faceMatchTvMatchingProgressStatus1.text =
                getString(R.string.face_match_loading_candidates)

            faceMatchProgress.isVisible = true
        }

        setIdentificationProgress(25)
    }

    private fun renderMatching() {
        binding.faceMatchTvMatchingProgressStatus1.text =
            getString(R.string.face_match_matching_candidates)

        setIdentificationProgress(50)
    }

    private fun renderFinished(matchState: Finished) {
        binding.faceMatchTvMatchingProgressStatus1.text = resources.getQuantityString(
            R.plurals.face_match_matched_candidates,
            matchState.candidatesMatched,
            matchState.candidatesMatched
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = resources.getQuantityString(
            R.plurals.face_match_returned_results,
            matchState.returnSize,
            matchState.returnSize
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = resources.getQuantityString(
                R.plurals.face_match_tier1or2_matches,
                matchState.veryGoodMatches,
                matchState.veryGoodMatches
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = resources.getQuantityString(
                R.plurals.face_match_tier3_matches,
                matchState.goodMatches,
                matchState.goodMatches
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = resources.getQuantityString(
                R.plurals.face_match_tier4_matches,
                matchState.fairMatches,
                matchState.fairMatches
            )
        }

        setIdentificationProgress(100)
    }

    companion object {
        fun getStartingIntent(context: Context, faceMatchRequest: FaceMatchRequest): Intent =
            Intent(context, FaceMatchActivity::class.java).apply {
                putExtra(IFaceRequest.BUNDLE_KEY, faceMatchRequest)
            }
    }
}
