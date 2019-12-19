package com.simprints.fingerprint.activities.matching

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.Observer
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForMatchingActivityException
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode.*
import kotlinx.android.synthetic.main.activity_matching.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MatchingActivity : FingerprintActivity() {

    private val viewModel: MatchingViewModel by viewModel()
    val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    private lateinit var matchingRequest: MatchingTaskRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        matchingRequest = this.intent.extras?.getParcelable(MatchingTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForMatchingActivityException()

        setContentView(R.layout.activity_matching)
        setTextInLayout()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        observeResult()
        observeProgress()
        observeTextViewUpdates()
        observeErrorHandlingCases()
    }

    private fun setTextInLayout() {
        matching_please_wait.text = androidResourcesHelper.getString(R.string.please_wait)
    }

    override fun onResume() {
        super.onResume()
        viewModel.start(matchingRequest)
    }

    private fun observeResult() {
        viewModel.result.observe(this, Observer {
            setResult(it.resultCode.value, it.data)
            Handler().postDelayed({ finish() }, it.finishDelayMillis.toLong())
        })
    }

    private fun observeProgress() {
        viewModel.progress.observe(this, Observer {
            setIdentificationProgress(it)
        })
    }

    private fun observeTextViewUpdates() {
        viewModel.hasLoadingBegun.observe(this, Observer {
            if (it) tv_matchingProgressStatus1.setText(R.string.loading_candidates)
        })

        viewModel.matchBeginningSummary.observe(this, Observer {
            tv_matchingProgressStatus1.text = androidResourcesHelper.getStringPlural(R.string.loaded_candidates_quantity_key, it.matchSize, arrayOf(it.matchSize))
            tv_matchingProgressStatus2.setText(R.string.matching_fingerprints)
        })

        viewModel.matchFinishedSummary.observe(this, Observer {
            tv_matchingProgressStatus2.text = androidResourcesHelper.getStringPlural(R.string.returned_results_quantity_key, it.returnSize, arrayOf(it.returnSize))

            if (it.veryGoodMatches > 0) {
                tv_matchingResultStatus1.visibility = View.VISIBLE
                tv_matchingResultStatus1.text = androidResourcesHelper.getStringPlural(R.string.tier1or2_matches_quantity_key, it.veryGoodMatches, arrayOf(it.veryGoodMatches))
            }
            if (it.goodMatches > 0) {
                tv_matchingResultStatus2.visibility = View.VISIBLE
                tv_matchingResultStatus2.text = androidResourcesHelper.getStringPlural(R.string.tier3_matches_quantity_key, it.goodMatches, arrayOf(it.goodMatches))
            }
            if (it.veryGoodMatches < 1 && it.goodMatches < 1 || it.fairMatches > 1) {
                tv_matchingResultStatus3.visibility = View.VISIBLE
                tv_matchingResultStatus3.text = androidResourcesHelper.getStringPlural(R.string.tier4_matches_quantity_key, it.fairMatches, arrayOf(it.fairMatches))
            }
            setIdentificationProgress(100)
        })
    }

    private fun observeErrorHandlingCases() {
        viewModel.alert.observe(this, Observer { launchAlert(this, it) })

        viewModel.hasMatchFailed.observe(this, Observer {
            if (it) {
                Toast.makeText(this@MatchingActivity, "Matching failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun setIdentificationProgress(progress: Int) =
        runOnUiThread {
            ObjectAnimator.ofInt(pb_identification, "progress", pb_identification.progress, progress)
                .setDuration((progress * 10).toLong())
                .start()
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (ResultCode.fromValue(resultCode)) {
            REFUSED -> setResultAndFinish(REFUSED, data)
            ALERT -> setResultAndFinish(ALERT, data)
            CANCELLED -> setResultAndFinish(CANCELLED, data)
            OK -> {
            }
        }
    }

    private fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
        finish()
    }

    override fun onBackPressed() {}
}
