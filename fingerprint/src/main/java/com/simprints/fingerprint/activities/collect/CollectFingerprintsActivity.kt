package com.simprints.fingerprint.activities.collect

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.views.TimeoutBar
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.ALERT_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.MATCHING_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.InternalConstants.RequestIntents.Companion.REFUSAL_ACTIVITY_REQUEST
import com.simprints.fingerprint.data.domain.InternalConstants.ResultIntents.Companion.ALERT_TRY_AGAIN_RESULT
import com.simprints.fingerprint.data.domain.alert.FingerprintAlert
import com.simprints.fingerprint.data.domain.collect.CollectResult
import com.simprints.fingerprint.data.domain.matching.request.MatchingActIdentifyRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActRequest
import com.simprints.fingerprint.data.domain.matching.request.MatchingActVerifyRequest
import com.simprints.fingerprint.data.domain.matching.result.MatchingActResult
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintIdentifyRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintRequest
import com.simprints.fingerprint.data.domain.moduleapi.fingerprint.requests.FingerprintVerifyRequest
import com.simprints.fingerprint.di.FingerprintComponentBuilder
import com.simprints.fingerprint.tools.extensions.launchAlert
import com.simprints.fingerprint.tools.extensions.launchRefusalActivity
import com.simprints.id.Application
import com.simprints.id.domain.fingerprint.Person
import kotlinx.android.synthetic.main.activity_collect_fingerprints.*
import kotlinx.android.synthetic.main.content_main.*

class CollectFingerprintsActivity :
    AppCompatActivity(),
    CollectFingerprintsContract.View {

    override lateinit var viewPresenter: CollectFingerprintsContract.Presenter

    override lateinit var viewPager: ViewPagerCustom
    override lateinit var indicatorLayout: LinearLayout
    override lateinit var pageAdapter: FingerPageAdapter
    override lateinit var scanButton: Button
    override lateinit var progressBar: ProgressBar
    override lateinit var timeoutBar: TimeoutBar
    override lateinit var un20WakeupDialog: ProgressDialog
    private lateinit var fingerprintRequest: FingerprintRequest

    private var rightToLeft: Boolean = false
    private val resultIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_fingerprints)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        fingerprintRequest = this.intent.extras?.getParcelable(FingerprintRequest.BUNDLE_KEY)
            ?: throw IllegalArgumentException("No AppRequest in the bundle") //STOPSHIP

        configureRightToLeft()

        val component = FingerprintComponentBuilder.getComponent(application as Application)
        viewPresenter = CollectFingerprintsPresenter(this, this, fingerprintRequest, component)
        initBar()
        initViewFields()
        viewPresenter.start()
    }

    private fun configureRightToLeft() {
        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    private fun initBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        supportActionBar?.title = viewPresenter.getTitle()
    }

    private fun initViewFields() {
        viewPager = view_pager
        indicatorLayout = indicator_layout
        scanButton = scan_button
        progressBar = pb_timeout
        setListenerToMissingFinger()
    }

    override fun onResume() {
        super.onResume()
        viewPresenter.handleOnResume()
    }

    override fun initViewPager(onPageSelected: (Int) -> Unit, onTouch: () -> Boolean) {
        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                onPageSelected(position)
            }
        })
        view_pager.setOnTouchListener { _, _ -> onTouch() }
        view_pager.currentItem = viewPresenter.currentActiveFingerNo

        reverseViewPagerIfNeeded()
    }

    private fun reverseViewPagerIfNeeded() {
        // If the layout is from right to left, we need to reverse the scrolling direction
        if (rightToLeft) view_pager.rotationY = 180f
    }

    private fun setListenerToMissingFinger() {
        missingFingerText.setOnClickListener { viewPresenter.handleMissingFingerClick() }
    }

    override fun refreshScanButtonAndTimeoutBar() {
        val activeStatus = viewPresenter.currentFinger().status
        scan_button.setText(activeStatus.buttonTextId)
        scan_button.setTextColor(activeStatus.buttonTextColor)
        scan_button.setBackgroundColor(ContextCompat.getColor(this, activeStatus.buttonBgColorRes))

        timeoutBar.setProgressBar(activeStatus)
    }

    override fun refreshFingerFragment() {
        pageAdapter.getFragment(viewPresenter.currentActiveFingerNo)?.let {
            reverseFingerFragmentIfNeeded(it)
            it.updateTextAccordingToStatus()
        }
    }

    private fun reverseFingerFragmentIfNeeded(it: FingerFragment) {
        // If the layout direction is RTL, then the view pager will have been rotated,
        // but the image and text need to be rotated back
        if (rightToLeft) {
            it.view?.rotationY = 180f
        }
    }

    override fun finishSuccessEnrol(bundleKey: String, result: CollectResult) =
        setCollectResultInIntent(bundleKey, result).also {
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

    override fun finishSuccessAndStartMatching(bundleKey: String, result: CollectResult) =
        setCollectResultInIntent(bundleKey, result).also {
            startMatchingActivity(result.probe)
        }

    private fun setCollectResultInIntent(bundleKey: String, result: CollectResult) {
        resultIntent.putExtra(bundleKey, result)
    }

    override fun startRefusalActivity() = launchRefusalActivity()

    private fun startMatchingActivity(probe: Person) {
        val matchingIntent = Intent(this, MatchingActivity::class.java)
        with(fingerprintRequest) {
            val extra = when {
                this is FingerprintVerifyRequest -> MatchingActVerifyRequest(
                        language,
                        probe,
                        projectId,
                        this.verifyGuid)
                this is FingerprintIdentifyRequest -> MatchingActIdentifyRequest(
                        language,
                        probe,
                        matchGroup,
                        returnIdCount)
                else -> null
            }
            matchingIntent.putExtra(MatchingActRequest.BUNDLE_KEY, extra)
        }

        startActivityForResult(matchingIntent, MATCHING_ACTIVITY_REQUEST)
    }


    override fun cancelAndFinish() =
        setResult(Activity.RESULT_CANCELED).also { finish() }

    override fun onBackPressed() {
        viewPresenter.handleOnBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        when (requestCode) {
            ALERT_ACTIVITY_REQUEST ->
                if (resultCode == ALERT_TRY_AGAIN_RESULT)
                    viewPresenter.handleTryAgainFromDifferentActivity()
                else setResult(Activity.RESULT_CANCELED)
            MATCHING_ACTIVITY_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val matchingResult = data?.getParcelableExtra<MatchingActResult>(MatchingActResult.BUNDLE_KEY)
                    resultIntent.putExtra(MatchingActResult.BUNDLE_KEY, matchingResult)
                }
                setResult(resultCode, resultIntent)
            }
            REFUSAL_ACTIVITY_REQUEST -> {
                setResult(resultCode, data)
            }
            else -> throw IllegalArgumentException("Invalid request Code")
        }.also { finish() }

    override fun onPause() {
        super.onPause()
        viewPresenter.handleOnPause()
    }

    override fun doLaunchAlert(alert: FingerprintAlert) {
        launchAlert(alert)
    }

    override fun showSplashScreen() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }
}
