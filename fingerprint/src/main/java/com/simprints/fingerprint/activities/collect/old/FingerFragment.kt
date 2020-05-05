package com.simprints.fingerprint.activities.collect.old

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.domain.Finger
import com.simprints.fingerprint.activities.collect.old.models.FingerRes
import com.simprints.fingerprint.activities.collect.resources.directionTextColour
import com.simprints.fingerprint.activities.collect.resources.directionTextId
import com.simprints.fingerprint.activities.collect.resources.resultTextColour
import com.simprints.fingerprint.activities.collect.resources.resultTextId
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.tools.extensions.activityIsPresentAndFragmentIsAdded
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FingerFragment : FingerprintFragment() {

    private val vm: CollectFingerprintsViewModel by sharedViewModel()

    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private val fingerprintPreferencesManager: FingerprintPreferencesManager by inject()

    lateinit var finger: Finger
    private lateinit var fingerImage: ImageView
    private lateinit var fingerResultText: TextView
    private lateinit var fingerDirectionText: TextView
    private lateinit var fingerNumberText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)

        finger = arguments?.get(FINGER_ARG) as Finger

        FingerRes.setFingerRes()

        fingerImage = view.findViewById(R.id.fingerImage)
        fingerResultText = view.findViewById(R.id.fingerResultText)
        fingerDirectionText = view.findViewById(R.id.fingerDirectionText)
        fingerNumberText = view.findViewById(R.id.fingerNumberText)

        if (activityIsPresentAndFragmentIsAdded()) {
            updateOrHideFingerImageAccordingToSettings()
            updateTextAccordingToStatus()
        }

        // TODO : Start listening to updates

        return view
    }

    private fun updateOrHideFingerImageAccordingToSettings() {
        if (fingerprintPreferencesManager.fingerImagesExist) {
            updateFingerImageAccordingToStatus()
        } else {
            fingerImage.visibility = View.INVISIBLE
        }
    }

    private fun updateFingerImageAccordingToStatus() {
        fingerImage.setImageResource(FingerRes.get(finger).drawableId)
        fingerImage.visibility = View.VISIBLE
    }

    fun updateTextAccordingToStatus() {
        updateFingerResultText()
        updateFingerNumberText()
        updateFingerDirectionText()
    }

    private fun updateFingerResultText() {
        with(vm.state.value?.fingerStates?.get(finger) ?: TODO("Oops")) {
            fingerResultText.text = androidResourcesHelper.getString(resultTextId())
            fingerResultText.setTextColor(getColor(requireContext(), resultTextColour()))
        }
    }

    private fun updateFingerNumberText() {
        fingerNumberText.text = androidResourcesHelper.getString(FingerRes.get(finger).nameId)
        fingerNumberText.setTextColor(getColor(requireContext(), R.color.simprints_blue))
    }

    private fun updateFingerDirectionText() {
        val isLastFinger = vm.state.value?.isOnLastFinger() ?: TODO("Oops")
        with(vm.state.value?.fingerStates?.get(finger) ?: TODO("Oops")) {
            fingerDirectionText.text = androidResourcesHelper.getString(directionTextId(isLastFinger))
            fingerDirectionText.setTextColor(getColor(requireContext(), directionTextColour()))
        }
    }

    companion object {

        private const val FINGER_ARG = "finger"

        fun newInstance(finger: Finger): FingerFragment {
            val fingerFragment = FingerFragment()
            val bundle = Bundle()
            bundle.putParcelable(FINGER_ARG, finger)
            fingerFragment.arguments = bundle
            return fingerFragment
        }
    }
}
