package com.simprints.fingerprint.activities.collect.fingerviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.base.FingerprintFragment
import com.simprints.fingerprint.activities.collect.CollectFingerprintsViewModel
import com.simprints.fingerprint.activities.collect.resources.*
import com.simprints.fingerprint.activities.collect.state.CollectFingerprintsState
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import kotlinx.android.synthetic.main.fragment_finger.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class FingerFragment : FingerprintFragment() {

    private val vm: CollectFingerprintsViewModel by sharedViewModel()

    private val androidResourcesHelper: FingerprintAndroidResourcesHelper by inject()
    private val fingerprintPreferencesManager: FingerprintPreferencesManager by inject()

    private lateinit var fingerId: FingerIdentifier

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)
        fingerId = FingerIdentifier.values()[arguments?.getInt(FINGER_ID_BUNDLE_KEY)
            ?: throw IllegalArgumentException()]
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
            fingerImage.setImageResource(fingerId.fingerDrawable())
        } else {
            fingerImage.visibility = View.INVISIBLE
        }
    }

    private fun updateFingerNameText() {
        fingerNumberText.text = androidResourcesHelper.getString(fingerId.nameTextId())
        fingerNumberText.setTextColor(resources.getColor(fingerId.nameTextColour(), null))
    }

    private fun CollectFingerprintsState.updateFingerResultText() {
        with(fingerStates.first { it.id == fingerId }) {
            fingerResultText.text = androidResourcesHelper.getString(resultTextId())
            fingerResultText.setTextColor(resources.getColor(resultTextColour(), null))
        }
    }

    private fun CollectFingerprintsState.updateFingerDirectionText() {
        with(fingerStates.first { it.id == fingerId }) {
            fingerDirectionText.text = androidResourcesHelper.getString(directionTextId(isOnLastFinger()))
            fingerDirectionText.setTextColor(resources.getColor(directionTextColour(), null))
        }
    }

    companion object {

        private const val FINGER_ID_BUNDLE_KEY = "finger_id"

        fun newInstance(fingerId: FingerIdentifier) = FingerFragment().also {
            it.arguments = Bundle().apply { putInt(FINGER_ID_BUNDLE_KEY, fingerId.ordinal) }
        }
    }
}
