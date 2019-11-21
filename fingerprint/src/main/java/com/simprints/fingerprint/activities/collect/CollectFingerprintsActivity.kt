package com.simprints.fingerprint.activities.collect

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.alert.AlertActivityHelper.launchAlert
import com.simprints.fingerprint.activities.alert.FingerprintAlert
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.result.CollectFingerprintsTaskResult
import com.simprints.fingerprint.activities.collect.views.TimeoutBar
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForCollectFingerprintsActivityException
import com.simprints.fingerprint.orchestrator.domain.RequestCode
import com.simprints.fingerprint.orchestrator.domain.ResultCode
import com.simprints.fingerprint.tools.extensions.launchRefusalActivity
import kotlinx.android.synthetic.main.activity_collect_fingerprints.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class CollectFingerprintsActivity :
    FingerprintActivity(),
    CollectFingerprintsContract.View {

    val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()

    private lateinit var fingerprintRequest: CollectFingerprintsTaskRequest
    override lateinit var viewPresenter: CollectFingerprintsContract.Presenter

    override lateinit var viewPager: ViewPagerCustom
    override lateinit var indicatorLayout: LinearLayout
    override lateinit var pageAdapter: FingerPageAdapter
    override lateinit var scanButton: Button
    override lateinit var progressBar: ProgressBar
    override lateinit var timeoutBar: TimeoutBar
    override lateinit var un20WakeupDialog: ProgressDialog

    private var rightToLeft: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_fingerprints)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        fingerprintRequest = this.intent.extras?.getParcelable(CollectFingerprintsTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForCollectFingerprintsActivityException()

        configureRightToLeft()

        viewPresenter = get { parametersOf(this, this, fingerprintRequest) }
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

        with(androidResourcesHelper) {
            scanButton.text = getString(R.string.scan)
            missingFingerText.text = getString(R.string.missing_finger)
            missingFingerText.paintFlags = missingFingerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
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
        scan_button.text = androidResourcesHelper.getString(activeStatus.buttonTextId)
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

    override fun setResultAndFinishSuccess(fingerprintsActResult: CollectFingerprintsTaskResult) {
        setResultAndFinish(ResultCode.OK, Intent().apply {
            putExtra(CollectFingerprintsTaskResult.BUNDLE_KEY, fingerprintsActResult)
        })
    }


    override fun startRefusalActivity() = launchRefusalActivity()

    override fun cancelAndFinish() =
        setResult(Activity.RESULT_CANCELED).also { finish() }

    override fun onBackPressed() {
        viewPresenter.handleOnBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.REFUSAL.value || requestCode == RequestCode.ALERT.value) {
            when (ResultCode.fromValue(resultCode)) {
                ResultCode.REFUSED -> setResultAndFinish(ResultCode.REFUSED, data)
                ResultCode.ALERT -> setResultAndFinish(ResultCode.ALERT, data)
                ResultCode.CANCELLED -> setResultAndFinish(ResultCode.CANCELLED, data)
                ResultCode.OK -> {
                    viewPresenter.handleTryAgainFromDifferentActivity()
                }
            }
        }
    }

    private fun setResultAndFinish(resultCode: ResultCode, data: Intent?) {
        setResult(resultCode.value, data)
        finish()
    }

    override fun onPause() {
        super.onPause()
        viewPresenter.handleOnPause()
    }

    override fun doLaunchAlert(fingerprintAlert: FingerprintAlert) {
        launchAlert(this, fingerprintAlert)
    }

    override fun showSplashScreen() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    override fun onDestroy() {
        viewPresenter.disconnectScannerIfNeeded()
        super.onDestroy()
    }
}
