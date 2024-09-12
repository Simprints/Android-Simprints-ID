package com.simprints.fingerprint.capture.views.fingerviewpager

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.capture.databinding.FragmentFingerBinding
import com.simprints.fingerprint.capture.databinding.FragmentFingerNewBinding
import com.simprints.fingerprint.capture.resources.captureNumberTextId
import com.simprints.fingerprint.capture.resources.directionTextColour
import com.simprints.fingerprint.capture.resources.directionTextId
import com.simprints.fingerprint.capture.resources.fingerDrawable
import com.simprints.fingerprint.capture.resources.nameTextColour
import com.simprints.fingerprint.capture.resources.nameTextId
import com.simprints.fingerprint.capture.resources.resultTextColour
import com.simprints.fingerprint.capture.resources.resultTextId
import com.simprints.fingerprint.capture.screen.FingerprintCaptureViewModel
import com.simprints.fingerprint.capture.state.CaptureState
import com.simprints.fingerprint.capture.state.CollectFingerprintsState
import com.simprints.fingerprint.capture.state.FingerState
import com.simprints.fingerprint.capture.views.timeoutbar.ScanCountdownBar
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class FingerFragment : Fragment(R.layout.fragment_finger_new) {

    private val binding by viewBinding(FragmentFingerNewBinding::bind)
    private val vm: FingerprintCaptureViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var fingerId: IFingerIdentifier

    private lateinit var timeoutBars: List<ScanCountdownBar>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fingerId = IFingerIdentifier.entries.toTypedArray()[arguments?.getInt(FINGER_ID_BUNDLE_KEY)
            ?: throw IllegalArgumentException()]

        initTimeoutBars()

        vm.stateLiveData.observe(viewLifecycleOwner) {
            updateOrHideFingerImageAccordingToSettings()
            updateFingerNameText()
//            updateFingerCaptureNumberText()
            updateFingerResultText()
            updateFingerDirectionText(it)
            updateTimeoutBars()
        }
    }

    private fun initTimeoutBars() = withFingerState {
        timeoutBars = List(captures.size) { index ->
            ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                progressDrawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.timer_progress_bar)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f / captures.size
                ).apply { if (index != 0) marginStart = PROGRESS_BAR_MARGIN }
            }
        }.map { progressBar ->
//            binding.progressBarContainer.addView(progressBar)
            ScanCountdownBar(binding.progressBarContainer, vm.progressBarTimeout())
        }
    }

    private inline fun withFingerState(crossinline block: FingerState.() -> Unit) = vm.stateLiveData.value
        ?.fingerStates
        ?.find { it.id == fingerId }
        ?.run(block)

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
    }

//    private fun updateFingerCaptureNumberText() = withFingerState {
//        if (isMultiCapture()) {
//            binding.fingerCaptureNumberText.setTextColor(
//                resources.getColor(
//                    nameTextColour(),
//                    null
//                )
//            )
//            binding.fingerCaptureNumberText.text =
//                getString(captureNumberTextId(), currentCaptureIndex + 1, captures.size)
//            binding.fingerCaptureNumberText.visibility = View.VISIBLE
//        } else {
//            binding.fingerCaptureNumberText.visibility = View.GONE
//        }
//    }


    private fun updateFingerResultText() = withFingerState {
        binding.fingerResultText.isVisible = currentCapture() !is CaptureState.Scanning
        binding.fingerResultText.text = getString(currentCapture().resultTextId())
        binding.fingerResultText.setTextColor(
            resources.getColor(
                currentCapture().resultTextColour(),
                null
            )
        )
    }

    private fun updateFingerDirectionText(state: CollectFingerprintsState) = withFingerState {
        binding.fingerResultText.isVisible = currentCapture() !is CaptureState.Scanning
        binding.fingerDirectionText.text = getString(directionTextId(state.isOnLastFinger()))
        binding.fingerDirectionText.setTextColor(
            resources.getColor(
                directionTextColour(),
                null
            )
        )
    }

    private fun updateTimeoutBars() = withFingerState {
        timeoutBars.forEachIndexed { captureIndex, timeoutBar ->
            with(timeoutBar) {
                val fingerState = captures[captureIndex]
                progressBar.isVisible = fingerState is CaptureState.Scanning
                when (fingerState) {
                    is CaptureState.NotCollected,
                    is CaptureState.Skipped -> {
                        handleCancelled()
                    }

                    is CaptureState.Scanning -> startTimeoutBar()
                    is CaptureState.TransferringImage -> {
                        //Do nothing
                    }

                    is CaptureState.NotDetected -> {
                        handleCancelled()
                    }

                    is CaptureState.Collected -> if (fingerState.scanResult.isGoodScan()) {
                        handleCancelled()
                    } else {
                        handleCancelled()
                    }
                }
            }
        }
    }

    companion object {

        private const val FINGER_ID_BUNDLE_KEY = "finger_id"

        private const val PROGRESS_BAR_MARGIN = 4

        fun newInstance(fingerId: IFingerIdentifier) = FingerFragment().also {
            it.arguments = Bundle().apply { putInt(FINGER_ID_BUNDLE_KEY, fingerId.ordinal) }
        }
    }
}
