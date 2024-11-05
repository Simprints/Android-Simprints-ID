package com.simprints.matcher.screen

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simprints.core.domain.permission.PermissionStatus
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.core.tools.extentions.hasPermission
import com.simprints.core.tools.extentions.permissionFromResult
import com.simprints.core.tools.extentions.applicationSettingsIntent
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.viewbinding.viewBinding
import com.simprints.matcher.R
import com.simprints.matcher.databinding.FragmentMatcherBinding
import com.simprints.matcher.screen.MatchViewModel.MatchState.*
import dagger.hilt.android.AndroidEntryPoint
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class MatchFragment : Fragment(R.layout.fragment_matcher) {

    private val viewModel: MatchViewModel by viewModels()
    private val binding by viewBinding(FragmentMatcherBinding::bind)
    private val args by navArgs<MatchFragmentArgs>()
    private var startTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val permissionCall = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val status = args.params.biometricDataSource.permissionName()
            ?.let { requireActivity().permissionFromResult(it, granted) }
            ?: PermissionStatus.Granted

        when (status) {
            PermissionStatus.Granted -> viewModel.setupMatch(args.params)
            PermissionStatus.Denied -> viewModel.noPermission(neverAskAgain = false)
            PermissionStatus.DeniedNeverAskAgain -> viewModel.noPermission(neverAskAgain = true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        val requiredPermissionName = args.params.biometricDataSource
            .permissionName()
            ?.takeUnless { requireActivity().hasPermission(it) }

        // This flag prevents infinite loop of permission checks
        if (viewModel.shouldCheckPermission) {
            viewModel.shouldCheckPermission = false

            if (requiredPermissionName != null) {
                permissionCall.launch(requiredPermissionName)
            } else {
                viewModel.setupMatch(args.params)
            }
        } else {
            viewModel.shouldCheckPermission = true
        }
    }

    private fun setIdentificationProgress(progress: Int) = requireActivity().runOnUiThread {
        ObjectAnimator
            .ofInt(binding.faceMatchProgress, "progress", binding.faceMatchProgress.progress, progress)
            .setDuration(progress * PROGRESS_DURATION_MULTIPLIER)
            .start()
    }

    private fun observeViewModel() {
        viewModel.matchResponse.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            findNavController().finishWithResult(this, it)
        })
        viewModel.matchState.observe(viewLifecycleOwner) { matchState ->
            when (matchState) {
                NotStarted -> renderNotStarted()
                LoadingCandidates -> renderLoadingCandidates()
                is Matching -> renderMatching()
                is Finished -> renderFinished(matchState)
                is NoPermission -> renderNoPermission(matchState)
            }
        }
        viewModel.timer.observe(viewLifecycleOwner) {
            if(it == true){
                startTimer()
            } else {
                stopTimer()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    private fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            handler.post(updateTimerRunnable)
            isRunning = true
        }
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(updateTimerRunnable)
    }
    // Runnable to update the timer UI every 10ms
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - startTime
                binding.timer.text = formatTime(elapsedTime)
                handler.postDelayed(this, 10) // Update every 10ms
            }
        }
    }
    // Function to format time in mm:ss:ms
    private fun formatTime(timeInMillis: Long): String {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val milliseconds = (timeInMillis % 1000) / 10
        return "Time elapsed:" + String.format("%02d:%02d:%02d", minutes, seconds, milliseconds)
    }
    private fun renderNotStarted() = binding.apply {
        binding.faceMatchPermissionRequestButton.isGone = true
        faceMatchTvMatchingProgressStatus1.isGone = true
        faceMatchTvMatchingProgressStatus2.isGone = true
        faceMatchProgress.isGone = true
        faceMatchTvMatchingResultStatus1.isGone = true
        faceMatchTvMatchingResultStatus2.isGone = true
        faceMatchTvMatchingResultStatus3.isGone = true
    }

    private fun renderLoadingCandidates() {
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.apply {
            faceMatchTvMatchingProgressStatus1.isVisible = true
            faceMatchTvMatchingProgressStatus1.setText(IDR.string.matcher_loading_candidates)
            faceMatchProgress.isVisible = true
        }
        setIdentificationProgress(LOADING_PROGRESS)
    }

    private fun renderMatching() {
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.faceMatchTvMatchingProgressStatus1.setText(IDR.string.matcher_matching_candidates)

        setIdentificationProgress(MATCHING_PROGRESS)
    }

    private fun renderFinished(matchState: Finished) {
        binding.faceMatchPermissionRequestButton.isVisible = false
        binding.faceMatchTvMatchingProgressStatus1.text = resources.getQuantityString(
            IDR.plurals.matcher_matched_candidates,
            matchState.candidatesMatched,
            matchState.candidatesMatched
        )

        binding.faceMatchTvMatchingProgressStatus2.isVisible = true
        binding.faceMatchTvMatchingProgressStatus2.text = resources.getQuantityString(
            IDR.plurals.matcher_returned_results,
            matchState.returnSize,
            matchState.returnSize
        )

        if (matchState.veryGoodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus1.isVisible = true
            binding.faceMatchTvMatchingResultStatus1.text = resources.getQuantityString(
                IDR.plurals.matcher_tier1or2_matches,
                matchState.veryGoodMatches,
                matchState.veryGoodMatches
            )
        }
        if (matchState.goodMatches > 0) {
            binding.faceMatchTvMatchingResultStatus2.isVisible = true
            binding.faceMatchTvMatchingResultStatus2.text = resources.getQuantityString(
                IDR.plurals.matcher_tier3_matches,
                matchState.goodMatches,
                matchState.goodMatches
            )
        }
        if (matchState.veryGoodMatches < 1 && matchState.goodMatches < 1 || matchState.fairMatches > 1) {
            binding.faceMatchTvMatchingResultStatus3.isVisible = true
            binding.faceMatchTvMatchingResultStatus3.text = resources.getQuantityString(
                IDR.plurals.matcher_tier4_matches,
                matchState.fairMatches,
                matchState.fairMatches
            )
        }

        setIdentificationProgress(MAX_PROGRESS)
    }

    private fun renderNoPermission(state: NoPermission) = with(binding) {
        faceMatchMessage.setText(IDR.string.matcher_missing_access_permission)

        val name = args.params.biometricDataSource.permissionName()
        faceMatchPermissionRequestButton.isVisible = name != null

        faceMatchPermissionRequestButton.setOnClickListener {
            if (state.shouldOpenSettings) startActivity(requireContext().applicationSettingsIntent)
            else permissionCall.launch(name)
        }
    }

    companion object {

        private const val MAX_PROGRESS = 100
        private const val PROGRESS_DURATION_MULTIPLIER = 10L
        private const val LOADING_PROGRESS = 25
        private const val MATCHING_PROGRESS = 50
    }
}
