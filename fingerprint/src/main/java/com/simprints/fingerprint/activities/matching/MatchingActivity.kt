package com.simprints.fingerprint.activities.matching

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simprints.core.tools.AndroidResourcesHelperImpl.Companion.getStringPlural
import com.simprints.core.tools.LanguageHelper
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.matching.request.MatchingTaskRequest
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.exceptions.FingerprintSimprintsException
import com.simprints.fingerprint.exceptions.unexpected.InvalidRequestForMatchingActivityException
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.extensions.logActivityCreated
import com.simprints.fingerprint.tools.extensions.logActivityDestroyed
import com.simprints.id.Application
import kotlinx.android.synthetic.main.activity_matching.*
import timber.log.Timber
import javax.inject.Inject

class MatchingActivity : AppCompatActivity(), MatchingContract.View {

    override lateinit var viewPresenter: MatchingContract.Presenter

    @Inject lateinit var dbManager: FingerprintDbManager
    @Inject lateinit var sessionEventsManager: FingerprintSessionEventsManager
    @Inject lateinit var crashReportManager: FingerprintCrashReportManager
    @Inject lateinit var timeHelper: FingerprintTimeHelper
    @Inject lateinit var preferencesManager: FingerprintPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logActivityCreated()
        val component = FingerprintComponentBuilder.getComponent(application as Application)
        component.inject(this)
        val matchingRequest: MatchingTaskRequest = this.intent.extras?.getParcelable(MatchingTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForMatchingActivityException()

        LanguageHelper.setLanguage(this, matchingRequest.language)

        setContentView(R.layout.activity_matching)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val extras = intent.extras
        if (extras == null) {
            crashReportManager.logExceptionOrSafeException(FingerprintSimprintsException("Null extras passed to MatchingActivity"))
            launchAlertActivity()
            return
        }

        viewPresenter = MatchingPresenter(this, matchingRequest, dbManager, sessionEventsManager, crashReportManager, preferencesManager, timeHelper)
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun setIdentificationProgress(progress: Int) =
        runOnUiThread {
            ObjectAnimator.ofInt(pb_identification, "progress", pb_identification.progress, progress)
                .setDuration((progress * 10).toLong())
                .start()
        }

    override fun setVerificationProgress() =
        runOnUiThread {
            setIdentificationProgress(100)
        }

    override fun setIdentificationProgressLoadingStart() =
        runOnUiThread {
            tv_matchingProgressStatus1.setText(R.string.loading_candidates)
            setIdentificationProgress(25)
        }

    override fun setIdentificationProgressMatchingStart(matchSize: Int) =
        runOnUiThread {
            tv_matchingProgressStatus1.text = getStringPlural(this@MatchingActivity, R.string.loaded_candidates_quantity_key, matchSize, matchSize)
            tv_matchingProgressStatus2.setText(R.string.matching_fingerprints)
            setIdentificationProgress(50)
        }

    override fun setIdentificationProgressReturningStart() =
        runOnUiThread {
            tv_matchingProgressStatus2.setText(R.string.returning_results)
            setIdentificationProgress(90)
        }

    override fun setIdentificationProgressFinished(returnSize: Int, tier1Or2Matches: Int, tier3Matches: Int, tier4Matches: Int, matchingEndWaitTimeMillis: Int) =
        runOnUiThread {
            tv_matchingProgressStatus2.text = getStringPlural(this@MatchingActivity, R.string.returned_results_quantity_key, returnSize, returnSize)

            if (tier1Or2Matches > 0) {
                tv_matchingResultStatus1.visibility = View.VISIBLE
                tv_matchingResultStatus1.text = getStringPlural(this@MatchingActivity, R.string.tier1or2_matches_quantity_key, tier1Or2Matches, tier1Or2Matches)
            }
            if (tier3Matches > 0) {
                tv_matchingResultStatus2.visibility = View.VISIBLE
                tv_matchingResultStatus2.text = getStringPlural(this@MatchingActivity, R.string.tier3_matches_quantity_key, tier3Matches, tier3Matches)
            }
            if (tier1Or2Matches < 1 && tier3Matches < 1 || tier4Matches > 1) {
                tv_matchingResultStatus3.visibility = View.VISIBLE
                tv_matchingResultStatus3.text = getStringPlural(this@MatchingActivity, R.string.tier4_matches_quantity_key, tier4Matches, tier4Matches)
            }
            setIdentificationProgress(100)

            val handler = Handler()
            handler.postDelayed({ finish() }, matchingEndWaitTimeMillis.toLong())
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (ResultCode.fromValue(resultCode)) {
            ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
            ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
            ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
            ResultCode.OK -> {
            }
        }
    }

    override fun launchAlertActivity() {
        launchAlert(this, FingerprintAlert.UNEXPECTED_ERROR)
    }

    override fun makeToastMatchFailed() {
        Toast.makeText(this@MatchingActivity, "Matching failed", Toast.LENGTH_LONG).show()
    }

    override fun setResultAndFinish(resultCode: ResultCode, resultData: Intent?) {
        doSetResult(resultCode, resultData)
        doFinish()
    }

    override fun doSetResult(resultCode: ResultCode, resultData: Intent?) {
        setResult(resultCode.value, resultData)
    }

    override fun doFinish() {
        Timber.d("MatchingAct: done")
        finish()
    }

    override fun onBackPressed() {
        viewPresenter.handleBackPressed()
    }

    override fun onDestroy() {
        viewPresenter.dispose()
        super.onDestroy()
        logActivityDestroyed()
    }
}
