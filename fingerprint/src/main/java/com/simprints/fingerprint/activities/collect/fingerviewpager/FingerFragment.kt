package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.domain.Finger
import com.simprints.fingerprint.activities.collect.resources.*
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import kotlinx.android.synthetic.main.fragment_finger.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FingerFragment : FingerprintFragment() {

    private val vm: CollectFingerprintsViewModel by sharedViewModel()

    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private val fingerprintPreferencesManager: FingerprintPreferencesManager by inject()

    lateinit var finger: Finger

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)
        finger = arguments?.get(FINGER_ARG) as Finger
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.state.fragmentObserveWith {
            updateOrHideFingerImageAccordingToSettings()
            updateFingerNameText()
            it.updateFingerResultText()
            it.updateFingerDirectionText()
        }
    }

    private fun updateOrHideFingerImageAccordingToSettings() {
        if (fingerprintPreferencesManager.fingerImagesExist) {
            fingerImage.visibility = View.VISIBLE
            fingerImage.setImageResource(finger.fingerDrawable())
        } else {
            fingerImage.visibility = View.INVISIBLE
        }
    }

    private fun updateFingerNameText() {
        fingerNumberText.text = androidResourcesHelper.getString(finger.nameTextId())
        fingerNumberText.setTextColor(resources.getColor(finger.nameTextColour(), null))
    }

    private fun CollectFingerprintsState.updateFingerResultText() {
        with(fingerStates.getValue(finger)) {
            fingerResultText.text = androidResourcesHelper.getString(resultTextId())
            fingerResultText.setTextColor(resources.getColor(resultTextColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateFingerDirectionText() {
        with(fingerStates.getValue(finger)) {
            fingerDirectionText.text = androidResourcesHelper.getString(directionTextId(isOnLastFinger()))
            fingerDirectionText.setTextColor(resources.getColor(directionTextColour(), null))
        }
    }

    companion object {

        private const val FINGER_ARG = "finger"

        fun newInstance(finger: Finger) = FingerFragment().also {
            it.arguments = Bundle().apply { putParcelable(FINGER_ARG, finger) }
        }
    }
}
