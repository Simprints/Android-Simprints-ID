package com.simprints.fingerprint.activities.collect

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintActivity
import com.simprints.fingerprint.activities.collect.old.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.old.FingerPageAdapter
import com.simprints.fingerprint.activities.collect.old.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.old.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.old.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.activities.collect.request.CollectFingerprintsTaskRequest
import com.simprints.fingerprint.activities.collect.resources.buttonBackgroundColour
import com.simprints.fingerprint.activities.collect.resources.buttonTextColour
import com.simprints.fingerprint.activities.collect.resources.buttonTextId
import com.simprints.fingerprint.activities.collect.resources.indicatorDrawableId
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.state.FingerCollectionState
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.flow.Action
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.data.domain.images.SaveFingerprintImagesStrategy
import com.simprints.fingerprint.exceptions.unexpected.request.InvalidRequestForCollectFingerprintsActivityException
import kotlinx.android.synthetic.main.activity_collect_fingerprints.*
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CollectFingerprintsActivity :
    FingerprintActivity() {

    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private val masterFlowManager: MasterFlowManager by inject()
    private val fingerprintPreferencesManager: FingerprintPreferencesManager by inject()

    private val vm: CollectFingerprintsViewModel by viewModel()

    private lateinit var pageAdapter: FingerPageAdapter
    private lateinit var timeoutBar: ScanningTimeoutBar
    private val indicators = ArrayList<ImageView>()
    private var rightToLeft: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_fingerprints)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val fingerprintRequest = this.intent.extras?.getParcelable<CollectFingerprintsTaskRequest>(CollectFingerprintsTaskRequest.BUNDLE_KEY)
            ?: throw InvalidRequestForCollectFingerprintsActivityException()

        vm.start(fingerprintRequest.fingerprintsToCapture)

        initUiComponents()
        observeStateChanges()
    }

    private fun initUiComponents() {
        configureRightToLeft()
        initToolbar()
        initPageAdapter()
        initViewPager()
        initIndicators()
        initTimeoutBar()
        initScanButton()
        initMissingFingerButton()
    }

    private fun configureRightToLeft() {
        rightToLeft = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.show()
        supportActionBar?.title = when (masterFlowManager.getCurrentAction()) {
            Action.ENROL -> androidResourcesHelper.getString(R.string.register_title)
            Action.IDENTIFY -> androidResourcesHelper.getString(R.string.identify_title)
            Action.VERIFY -> androidResourcesHelper.getString(R.string.verify_title)
        }
    }

    private fun initMissingFingerButton() {
        with(androidResourcesHelper) {
            missingFingerText.text = getString(R.string.missing_finger)
            missingFingerText.paintFlags = missingFingerText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
        missingFingerText.setOnClickListener { vm.handleMissingFingerButtonPressed() }
    }

    private fun initScanButton() {
        scan_button.setOnClickListener { vm.handleScanButtonPressed() }
    }

    private fun initTimeoutBar() {
        timeoutBar = if (isImageTransferRequired()) {
            ScanningWithImageTransferTimeoutBar(pb_timeout, CollectFingerprintsPresenter.scanningTimeoutMs, CollectFingerprintsPresenter.imageTransferTimeoutMs)
        } else {
            ScanningOnlyTimeoutBar(pb_timeout, CollectFingerprintsPresenter.scanningTimeoutMs)
        }
    }

    private fun isImageTransferRequired(): Boolean =
        when (fingerprintPreferencesManager.saveFingerprintImagesStrategy) {
            SaveFingerprintImagesStrategy.NEVER -> false
            SaveFingerprintImagesStrategy.WSQ_15 -> true
        }

    private fun initIndicators() {
        indicator_layout.removeAllViewsInLayout()
        indicators.clear()
        vm.state().orderedFingers().forEachIndexed { index, _ ->
            val indicator = ImageView(this)
            indicator.adjustViewBounds = true
            indicator.setOnClickListener {
                if (!vm.state().currentFingerState().isBusy()) {
                    vm.updateSelectedFingerIfNotBusy(index)
                }
            }
            indicators.add(indicator)
            indicator_layout.addView(indicator, LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        }
    }

    private fun initPageAdapter() {
        pageAdapter = FingerPageAdapter(
            supportFragmentManager,
            vm.state().orderedFingers()
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager() {
        view_pager.adapter = pageAdapter
        view_pager.offscreenPageLimit = 1
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                vm.updateSelectedFingerIfNotBusy(position)
            }
        })
        view_pager.setOnTouchListener { _, _ -> vm.state().currentFingerState().isBusy() }

        // If the layout is from right to left, we need to reverse the scrolling direction
        if (rightToLeft) view_pager.rotationY = 180f
    }

    private fun observeStateChanges() {
        vm.state.activityObserveWith {
            it.updateIndicators()
            it.updateScanButton()
            it.updateViewPager()
            it.updateProgressBar()
        }
    }

    private fun CollectFingerprintsState.updateIndicators() {
        orderedFingers().forEachIndexed { index, finger ->
            val selected = currentFingerIndex == index
            indicators[index].setImageResource(fingerStates.getValue(finger).indicatorDrawableId(selected))
        }
    }

    private fun CollectFingerprintsState.updateScanButton() {
        with(currentFingerState()) {
            scan_button.text = androidResourcesHelper.getString(buttonTextId())
            scan_button.setTextColor(resources.getColor(buttonTextColour(), null))
            scan_button.setBackgroundColor(resources.getColor(buttonBackgroundColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateViewPager() {
        view_pager.currentItem = currentFingerIndex

        // If the layout is has been rotated for RtL, we need to rotate the fragment back so it's upright
        if (rightToLeft) {
            pageAdapter.getFragment(currentFingerIndex)?.view?.rotationY = 180f
        }
    }

    private fun CollectFingerprintsState.updateProgressBar() {
        when (val fingerState = currentFingerState()) {
            FingerCollectionState.NotCollected,
            FingerCollectionState.Skipped -> {
                timeoutBar.progressBar.progress = 0
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bar)
            }
            FingerCollectionState.Scanning -> timeoutBar.startTimeoutBar()
            FingerCollectionState.TransferringImage -> timeoutBar.handleScanningFinished()
            FingerCollectionState.NotDetected -> {
                timeoutBar.progressBar.progress = 0
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
            }
            is FingerCollectionState.Collected -> if (fingerState.fingerScanResult.isGoodScan()) {
                timeoutBar.progressBar.progress = 0
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_good)
            } else {
                timeoutBar.progressBar.progress = 0
                timeoutBar.progressBar.progressDrawable = getDrawable(R.drawable.timer_progress_bad)
            }
        }
    }
}
