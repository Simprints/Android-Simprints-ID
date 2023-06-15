package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.resources.*
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningOnlyTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningTimeoutBar
import com.simprints.fingerprint.activities.collect.timeoutbar.ScanningWithImageTransferTimeoutBar
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import com.simprints.fingerprint.databinding.FragmentFingerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FingerFragment : FingerprintFragment() {

    private val vm: CollectFingerprintsViewModel by activityViewModels()
    private val binding by viewBinding(FragmentFingerBinding::bind)

    private lateinit var fingerId: FingerIdentifier

    private lateinit var timeoutBars: List<ScanningTimeoutBar>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)
        fingerId = FingerIdentifier.values()[arguments?.getInt(FINGER_ID_BUNDLE_KEY)
            ?: throw IllegalArgumentException()]
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTimeoutBars()

        vm.stateLiveData.fragmentObserveWith {
            updateOrHideFingerImageAccordingToSettings()
            updateFingerNameText()
            it.updateFingerCaptureNumberText()
            it.updateFingerResultText()
            it.updateFingerDirectionText()
            it.updateTimeoutBars()
        }
    }

    private fun initTimeoutBars() {
        vm.state.fingerStates.find { it.id == fingerId }?.run {
            timeoutBars = List(captures.size) {
                ProgressBar(
                    requireContext(),
                    null,
                    android.R.attr.progressBarStyleHorizontal
                ).apply {
                    progressDrawable =
                        ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bar)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f / captures.size
                    )
                        .apply { if (it != 0) marginStart = PROGRESS_BAR_MARGIN }
                }
            }.map { progressBar ->
                binding.progressBarContainer.addView(progressBar)
                if (vm.isImageTransferRequired()) {
                    ScanningWithImageTransferTimeoutBar(
                        progressBar,
                        CollectFingerprintsViewModel.scanningTimeoutMs,
                        CollectFingerprintsViewModel.imageTransferTimeoutMs
                    )
                } else {
                    ScanningOnlyTimeoutBar(
                        progressBar,
                        CollectFingerprintsViewModel.scanningTimeoutMs
                    )
                }
            }
        }
    }

    private fun updateOrHideFingerImageAccordingToSettings() {
        if (vm.configuration.displayHandIcons) {
            binding.fingerImage.visibility = View.VISIBLE
            binding.fingerImage.setImageResource(fingerId.fingerDrawable())
        } else {
            binding.fingerImage.visibility = View.INVISIBLE
        }
    }

    private fun updateFingerNameText() {
        binding.fingerNumberText.text = getString(fingerId.nameTextId())
        binding.fingerNumberText.setTextColor(resources.getColor(fingerId.nameTextColour(), null))
    }

    private fun CollectFingerprintsState.updateFingerCaptureNumberText() {
        fingerStates.find { it.id == fingerId }?.run {
            if (isMultiCapture()) {
                binding.fingerCaptureNumberText.setTextColor(
                    resources.getColor(
                        nameTextColour(),
                        null
                    )
                )
                binding.fingerCaptureNumberText.text =
                    getString(captureNumberTextId(), currentCaptureIndex + 1, captures.size)
                binding.fingerCaptureNumberText.visibility = View.VISIBLE
            } else {
                binding.fingerCaptureNumberText.visibility = View.GONE
            }
        }
    }

    private fun CollectFingerprintsState.updateFingerResultText() {
        fingerStates.find { it.id == fingerId }?.run {
            binding.fingerResultText.text = getString(currentCapture().resultTextId())
            binding.fingerResultText.setTextColor(
                resources.getColor(
                    currentCapture().resultTextColour(),
                    null
                )
            )
        }
    }

    private fun CollectFingerprintsState.updateFingerDirectionText() {
        fingerStates.find { it.id == fingerId }?.run {
            binding.fingerDirectionText.text = getString(directionTextId(isOnLastFinger()))
            binding.fingerDirectionText.setTextColor(
                resources.getColor(
                    directionTextColour(),
                    null
                )
            )
        }
    }

    private fun CollectFingerprintsState.updateTimeoutBars() {
        fingerStates.find { it.id == fingerId }?.run {
            timeoutBars.forEachIndexed { captureIndex, timeoutBar ->
                with(timeoutBar) {
                    when (val fingerState = captures[captureIndex]) {
                        is CaptureState.NotCollected,
                        is CaptureState.Skipped -> {
                            handleCancelled()
                            progressBar.progressDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.timer_progress_bar
                            )
                        }
                        is CaptureState.Scanning -> startTimeoutBar()
                        is CaptureState.TransferringImage -> handleScanningFinished()
                        is CaptureState.NotDetected -> {
                            handleCancelled()
                            progressBar.progressDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.timer_progress_bad
                            )
                        }
                        is CaptureState.Collected -> if (fingerState.scanResult.isGoodScan()) {
                            handleCancelled()
                            progressBar.progressDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.timer_progress_good
                            )
                        } else {
                            handleCancelled()
                            progressBar.progressDrawable = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.timer_progress_bad
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {

        private const val FINGER_ID_BUNDLE_KEY = "finger_id"

        private const val PROGRESS_BAR_MARGIN = 4

        fun newInstance(fingerId: FingerIdentifier) = FingerFragment().also {
            it.arguments = Bundle().apply { putInt(FINGER_ID_BUNDLE_KEY, fingerId.ordinal) }
        }
    }
}
