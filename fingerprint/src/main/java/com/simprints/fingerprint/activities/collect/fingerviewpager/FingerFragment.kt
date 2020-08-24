package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.resources.*
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import kotlinx.android.synthetic.main.fragment_finger.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FingerFragment : FingerprintFragment() {

    private val vm: CollectFingerprintsViewModel by sharedViewModel()

    private val fingerprintPreferencesManager: FingerprintPreferencesManager by inject()

    private lateinit var fingerId: FingerIdentifier

    private lateinit var timeoutBar: ScanningTimeoutBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)
        fingerId = FingerIdentifier.values()[arguments?.getInt(FINGER_ID_BUNDLE_KEY)
            ?: throw IllegalArgumentException()]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTimeoutBar()

        vm.state.fragmentObserveWith {
            updateOrHideFingerImageAccordingToSettings()
            updateFingerNameText()
            it.updateFingerCaptureNumberText()
            it.updateFingerResultText()
            it.updateFingerDirectionText()
            it.updateProgressBar()
        }
    }

    private fun initTimeoutBar() {
        val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
            progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bar)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        progressBarContainer.addView(progressBar)
        timeoutBar = if (vm.isImageTransferRequired()) {
            ScanningWithImageTransferTimeoutBar(progressBar, CollectFingerprintsViewModel.scanningTimeoutMs, CollectFingerprintsViewModel.imageTransferTimeoutMs)
        } else {
            ScanningOnlyTimeoutBar(progressBar, CollectFingerprintsViewModel.scanningTimeoutMs)
        }
    }

    private fun updateOrHideFingerImageAccordingToSettings() {
        if (fingerprintPreferencesManager.fingerImagesExist) {
            fingerImage.visibility = View.VISIBLE
            fingerImage.setImageResource(fingerId.fingerDrawable())
        } else {
            fingerImage.visibility = View.INVISIBLE
        }
    }

    private fun updateFingerNameText() {
        fingerNumberText.text = getString(fingerId.nameTextId())
        fingerNumberText.setTextColor(resources.getColor(fingerId.nameTextColour(), null))
    }

    private fun CollectFingerprintsState.updateFingerCaptureNumberText() {
        fingerStates.find { it.id == fingerId }?.run {
            if (isMultiCapture()) {
                fingerCaptureNumberText.setTextColor(resources.getColor(nameTextColour(), null))
                fingerCaptureNumberText.text = getString(captureNumberTextId(), currentCaptureIndex + 1, captures.size)
                fingerCaptureNumberText.visibility = View.VISIBLE
            } else {
                fingerCaptureNumberText.visibility = View.GONE
            }
        }
    }

    private fun CollectFingerprintsState.updateFingerResultText() {
        fingerStates.find { it.id == fingerId }?.run {
            fingerResultText.text = getString(currentCapture().resultTextId())
            fingerResultText.setTextColor(resources.getColor(currentCapture().resultTextColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateFingerDirectionText() {
        fingerStates.find { it.id == fingerId }?.run {
            fingerDirectionText.text = getString(directionTextId(isOnLastFinger()))
            fingerDirectionText.setTextColor(resources.getColor(directionTextColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateProgressBar() {
        with(timeoutBar) {
            when (val fingerState = currentCaptureState()) {
                is CaptureState.NotCollected,
                is CaptureState.Skipped -> {
                    handleCancelled()
                    progressBar.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bar)
                }
                is CaptureState.Scanning -> startTimeoutBar()
                is CaptureState.TransferringImage -> handleScanningFinished()
                is CaptureState.NotDetected -> {
                    handleCancelled()
                    progressBar.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bad)
                }
                is CaptureState.Collected -> if (fingerState.scanResult.isGoodScan()) {
                    handleCancelled()
                    progressBar.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_good)
                } else {
                    handleCancelled()
                    progressBar.progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bad)
                }
            }
        }
    }

    companion object {

        private const val FINGER_ID_BUNDLE_KEY = "finger_id"

        fun newInstance(fingerId: FingerIdentifier) = FingerFragment().also {
            it.arguments = Bundle().apply { putInt(FINGER_ID_BUNDLE_KEY, fingerId.ordinal) }
        }
    }
}
