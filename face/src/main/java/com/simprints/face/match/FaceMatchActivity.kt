package com.simprints.face.match

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.getStringPlural
import com.simprints.face.R
import com.simprints.face.base.FaceActivity
import com.simprints.face.data.moduleapi.face.requests.FaceMatchRequest
import com.simprints.face.databinding.ActivityFaceMatchBinding
import com.simprints.face.exceptions.InvalidFaceRequestException
import com.simprints.face.match.FaceMatchViewModel.MatchState.*
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import org.koin.android.viewmodel.ext.android.viewModel

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class FaceMatchActivity : FaceActivity() {
    private val vm: FaceMatchViewModel by viewModel()
    private lateinit var binding: ActivityFaceMatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFaceMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTextInLayout()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
            Handler().postDelayed({ finish() }, FaceMatchViewModel.matchingEndWaitTimeInMillis)
        })
        vm.matchState.observe(this, Observer { matchState ->
            when (matchState) {
                NotStarted -> renderNotStarted()
                Error -> TODO()
                LoadingCandidates -> renderLoadingCandidates()
                is Matching -> renderMatching()
                is Finished -> renderFinished(matchState)
            }
        })
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
        binding.faceMatchTvMatchingProgressStatus1.text = getStringPlural(
            R.string.face_match_matched_candidates_quantity_key,
            matchState.candidatesMatched,
            arrayOf(matchState.candidatesMatched)
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = getStringPlural(
            R.string.face_match_returned_results_quantity_key,
            matchState.returnSize,
            arrayOf(matchState.returnSize)
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = getStringPlural(
                R.string.face_match_tier1or2_matches_quantity_key,
                matchState.veryGoodMatches,
                arrayOf(matchState.veryGoodMatches)
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = getStringPlural(
                R.string.face_match_tier3_matches_quantity_key,
                matchState.goodMatches,
                arrayOf(matchState.goodMatches)
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = getStringPlural(
                R.string.face_match_tier4_matches_quantity_key,
                matchState.fairMatches,
                arrayOf(matchState.fairMatches)
            )
        }

        setIdentificationProgress(100)
    }

    override fun onBackPressed() {}

    companion object {
        fun getStartingIntent(context: Context, faceMatchRequest: FaceMatchRequest): Intent =
            Intent(context, FaceMatchActivity::class.java).apply {
                putExtra(IFaceRequest.BUNDLE_KEY, faceMatchRequest)
            }
    }
}
