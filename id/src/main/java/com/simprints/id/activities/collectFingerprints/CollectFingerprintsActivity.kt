package com.simprints.id.activities.collectFingerprints

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.simprints.id.R
import com.simprints.id.activities.refusal.RefusalActivity
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.tools.InternalConstants.REFUSAL_ACTIVITY_REQUEST
import com.simprints.id.tools.InternalConstants.RESULT_TRY_AGAIN
import com.simprints.id.tools.TimeoutBar
import com.simprints.id.tools.extensions.launchAlert
import kotlinx.android.synthetic.main.activity_main.*

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

    private var rightToLeft: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        configureRightToLeft()

        viewPresenter = CollectFingerprintsPresenter(this, this)
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
    }

    override fun onStart() {
        super.onStart()
        viewPresenter.handleOnStart()
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

    override fun refreshScanButtonAndTimeoutBar() {
        val activeStatus = viewPresenter.currentFinger().status
        scan_button.setText(activeStatus.buttonTextId)
        scan_button.setTextColor(activeStatus.buttonTextColor)
        scan_button.setBackgroundColor(activeStatus.buttonBgColor)

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

    override fun finishSuccessEnrol(result: Intent) =
        setResultAndFinish(Activity.RESULT_OK, result)

    override fun finishSuccessAndStartMatching(intent: Intent) =
        startActivityForResult(intent, CollectFingerprintsActivity.MATCHING_ACTIVITY_REQUEST_CODE)

    override fun cancelAndFinish() =
        setResultAndFinish(Activity.RESULT_CANCELED)

    override fun onBackPressed() {
        when {
            viewPresenter.isScanning() -> viewPresenter.handleBackPressedWhileScanning()
            else -> {
                viewPresenter.handleOnBackPressedToLeave()
                startActivityForResult(Intent(this, RefusalActivity::class.java), REFUSAL_ACTIVITY_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
        when (requestCode) {
            SETTINGS_ACTIVITY_REQUEST_CODE,
            PRIVACY_ACTIVITY_REQUEST_CODE,
            ABOUT_ACTIVITY_REQUEST_CODE ->
                super.onActivityResult(requestCode, resultCode, data)
            REFUSAL_ACTIVITY_REQUEST, ALERT_ACTIVITY_REQUEST_CODE ->
                if (resultCode == RESULT_TRY_AGAIN)
                    viewPresenter.handleTryAgainFromDifferentActivity()
                else
                    setResultAndFinish(resultCode, data)
            else -> setResultAndFinish(resultCode, data)
        }

    private fun setResultAndFinish(resultCode: Int, data: Intent? = null) {
        setResult(resultCode, data)
        finish()
    }

    override fun onStop() {
        super.onStop()
        viewPresenter.handleOnStop()
    }

    override fun doLaunchAlert(alertType: ALERT_TYPE) {
        launchAlert(alertType)
    }

    override fun showSplashScreen() {
        startActivity(Intent(this, SplashScreenActivity::class.java))
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
    }

    companion object {
        private const val ALERT_ACTIVITY_REQUEST_CODE = 0
        private const val MATCHING_ACTIVITY_REQUEST_CODE = 1
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 2
        private const val PRIVACY_ACTIVITY_REQUEST_CODE = 3
        private const val ABOUT_ACTIVITY_REQUEST_CODE = 4
    }
}
