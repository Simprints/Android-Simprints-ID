package com.simprints.fingerprints.activities.matching

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simprints.fingerprints.di.FingerprintsComponentBuilder
import com.simprints.id.Application
import com.simprints.id.activities.IntentKeys
import com.simprints.id.activities.alert.AlertActivity
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.domain.requests.Request
import com.simprints.id.exceptions.safe.callout.NoIntentExtrasError
import com.simprints.id.tools.LanguageHelper
import com.simprints.id.tools.utils.AndroidResourcesHelperImpl.Companion.getStringPlural
import kotlinx.android.synthetic.main.activity_matching.*
import javax.inject.Inject
import com.simprints.fingerprints.R
import com.simprints.id.R as appR //STOPSHIP: remove me

class MatchingActivity : AppCompatActivity(), MatchingContract.View {

    override lateinit var viewPresenter: MatchingContract.Presenter

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = FingerprintsComponentBuilder.getComponent(application as Application)
        component.inject(this)

        LanguageHelper.setLanguage(this, preferencesManager.language)
        setContentView(R.layout.activity_matching)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Create the Presenter, and pass it all the information and handles it needs
        val extras = intent.extras
        if (extras == null) {
            crashReportManager.logExceptionOrThrowable(NoIntentExtrasError("Null extras passed to MatchingActivity"))
            launchAlert()
            finish()
            return
        }

        val probe = extras.getParcelable<Person>(IntentKeys.matchingActivityProbePersonKey)
            ?: throw IllegalArgumentException("No probe in the bundle") //STOPSHIP

        val appRequest: Request = this.intent.extras?.getParcelable(Request.BUNDLE_KEY)
            ?: throw IllegalArgumentException("No request in the bundle") //STOPSHIP

        viewPresenter = MatchingPresenter(component, this, probe, appRequest)
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.start()
    }

    @SuppressLint("ObjectAnimatorBinding")
    override fun setIdentificationProgress(progress: Int) {
        ObjectAnimator.ofInt(pb_identification, "progress", pb_identification.progress, progress)
            .setDuration((progress * 10).toLong())
            .start()
    }

    override fun setVerificationProgress() {
        setIdentificationProgress(100)
    }

    override fun setIdentificationProgressLoadingStart() {
        tv_matchingProgressStatus1.setText(appR.string.loading_candidates)
        setIdentificationProgress(25)
    }

    override fun setIdentificationProgressMatchingStart(matchSize: Int) {
        tv_matchingProgressStatus1.text = getStringPlural(this@MatchingActivity, appR.string.loaded_candidates_quantity_key, matchSize, matchSize)
        tv_matchingProgressStatus2.setText(appR.string.matching_fingerprints)
        setIdentificationProgress(50)
    }

    override fun setIdentificationProgressReturningStart() {
        tv_matchingProgressStatus2.setText(appR.string.returning_results)
        setIdentificationProgress(90)
    }

    override fun setIdentificationProgressFinished(returnSize: Int, tier1Or2Matches: Int, tier3Matches: Int, tier4Matches: Int, matchingEndWaitTimeMillis: Int) {
        tv_matchingProgressStatus2.text = getStringPlural(this@MatchingActivity, appR.string.returned_results_quantity_key, returnSize, returnSize)

        if (tier1Or2Matches > 0) {
            tv_matchingResultStatus1.visibility = View.VISIBLE
            tv_matchingResultStatus1.text = getStringPlural(this@MatchingActivity, appR.string.tier1or2_matches_quantity_key, tier1Or2Matches, tier1Or2Matches)
        }
        if (tier3Matches > 0) {
            tv_matchingResultStatus2.visibility = View.VISIBLE
            tv_matchingResultStatus2.text = getStringPlural(this@MatchingActivity, appR.string.tier3_matches_quantity_key, tier3Matches, tier3Matches)
        }
        if (tier1Or2Matches < 1 && tier3Matches < 1 || tier4Matches > 1) {
            tv_matchingResultStatus3.visibility = View.VISIBLE
            tv_matchingResultStatus3.text = getStringPlural(this@MatchingActivity, appR.string.tier4_matches_quantity_key, tier4Matches, tier4Matches)
        }
        setIdentificationProgress(100)

        val handler = Handler()
        handler.postDelayed({ finish() }, matchingEndWaitTimeMillis.toLong())
    }

    override fun launchAlert() {
        val intent = Intent(this, AlertActivity::class.java)
        intent.putExtra(IntentKeys.alertActivityAlertTypeKey, ALERT_TYPE.UNEXPECTED_ERROR)
        startActivityForResult(intent, ALERT_ACTIVITY_REQUEST_CODE)
    }

    override fun makeToastMatchNotRunning(text: String) {
        Toast.makeText(this@MatchingActivity, text, Toast.LENGTH_LONG).show()
    }

    override fun doSetResult(resultCode: Int, resultData: Intent) {
        setResult(resultCode, resultData)
    }

    override fun doFinish() {
        finish()
    }

    override fun onDestroy() {
        viewPresenter.dispose()
        super.onDestroy()
    }

    companion object {
        private const val ALERT_ACTIVITY_REQUEST_CODE = 0
    }
}
