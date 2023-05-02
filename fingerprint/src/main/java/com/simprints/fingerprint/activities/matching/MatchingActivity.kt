package com.simprints.fingerprint.activities.matching

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import com.simprints.core.tools.viewbinding.viewBinding
import com.simprints.feature.alert.ShowAlertWrapper
import com.simprints.feature.alert.toArgs
import com.simprints.feature.exitform.ShowExitFormWrapper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.activities.refusal.RefusalAlertHelper
import com.simprints.fingerprint.databinding.ActivityMatchingBinding
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForMatchingActivityException
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingActivity : FingerprintActivity() {

    private val viewModel: MatchingViewModel by viewModels()
    private val binding by viewBinding(ActivityMatchingBinding::inflate)

    private lateinit var matchingRequest: MatchingTaskRequest

    private val showRefusal = registerForActivityResult(ShowExitFormWrapper()) { data ->
        RefusalAlertHelper.handleRefusal(
            data = data,
            onSubmit = { setResultAndFinish(REFUSED, it) },
        )
    }

    private val alertHelper = AlertActivityHelper()
    private val showAlert = registerForActivityResult(ShowAlertWrapper()) { data ->
        alertHelper.handleAlertResult(
            this,
            data,
            showRefusal = { showRefusal.launch(RefusalAlertHelper.refusalArgs()) },
            retry = { },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        matchingRequest = this.intent.extras?.getParcelable(MatchingTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForMatchingActivityException()

        setContentView(binding.root)

        setTextInLayout()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        observeResult()
        observeProgress()
        observeTextViewUpdates()
        observeErrorHandlingCases()
    }

    private fun setTextInLayout() {
        binding.matchingPleaseWait.text = getString(R.string.please_wait)
    }

    override fun onResume() {
        super.onResume()
        viewModel.start(matchingRequest)
    }

    private fun observeResult() {
        viewModel.result.observe(this) {
            setResult(it.resultCode.value, it.data)
            Handler(Looper.getMainLooper()).postDelayed({ finish() }, it.finishDelayMillis.toLong())
        }
    }

    private fun observeProgress() {
        viewModel.progress.observe(this) {
            setIdentificationProgress(it)
        }
    }

    private fun observeTextViewUpdates() {
        viewModel.hasLoadingBegun.observe(this) {
            if (it) binding.tvMatchingProgressStatus1.setText(R.string.loading_candidates)
        }

        viewModel.matchBeginningSummary.observe(this) {
            binding.tvMatchingProgressStatus1.text = resources.getQuantityString(
                R.plurals.loaded_candidates_result,
                it.matchSize,
                it.matchSize
            )
            binding.tvMatchingProgressStatus2.setText(R.string.matching_fingerprints)
        }

        viewModel.matchFinishedSummary.observe(this) {
            binding.tvMatchingProgressStatus2.text = resources.getQuantityString(
                R.plurals.returned_results,
                it.returnSize,
                it.returnSize
            )

            if (it.veryGoodMatches > 0) {
                binding.tvMatchingResultStatus1.visibility = View.VISIBLE
                binding.tvMatchingResultStatus1.text = resources.getQuantityString(
                    R.plurals.tier1or2_matches,
                    it.veryGoodMatches,
                    it.veryGoodMatches
                )
            }
            if (it.goodMatches > 0) {
                binding.tvMatchingResultStatus2.visibility = View.VISIBLE
                binding.tvMatchingResultStatus2.text = resources.getQuantityString(
                    R.plurals.tier3_matches,
                    it.goodMatches,
                    it.goodMatches
                )
            }
            if (it.veryGoodMatches < 1 && it.goodMatches < 1 || it.fairMatches > 1) {
                binding.tvMatchingResultStatus3.visibility = View.VISIBLE
                binding.tvMatchingResultStatus3.text = resources.getQuantityString(
                    R.plurals.tier4_matches,
                    it.fairMatches,
                    it.fairMatches
                )
            }
            setIdentificationProgress(100)
        }
    }

    private fun observeErrorHandlingCases() {
        viewModel.alert.observe(this) { showAlert.launch(it.toAlertConfig().toArgs()) }

        viewModel.hasMatchFailed.observe(this) {
            if (it) {
                Toast.makeText(this@MatchingActivity, "Matching failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun setIdentificationProgress(progress: Int) =
        runOnUiThread {
            ObjectAnimator.ofInt(
                binding.pbIdentification,
                "progress",
                binding.pbIdentification.progress,
                progress
            )
                .setDuration((progress * 10).toLong())
                .start()
        }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun onBackPressed() {}
}
