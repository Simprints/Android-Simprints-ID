package com.simprints.fingerprint.activities.collect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.simprints.fingerprint.R
import com.simprints.fingerprint.activities.collect.models.Finger
import com.simprints.fingerprint.activities.collect.models.FingerRes
import com.simprints.fingerprint.activities.collect.models.FingerStatus
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.tools.extensions.activityIsPresentAndFragmentIsAdded

class FingerFragment : Fragment() {

    lateinit var androidResourcesHelper: FingerprintAndroidResourcesHelper
    lateinit var fingerprintPreferencesManager: FingerprintPreferencesManager

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
        fingerResultText.text = androidResourcesHelper.getString(finger.status.textResult)
        fingerResultText.setTextColor(getColor(requireContext(), finger.status.textResultColorRes))
    }

    private fun updateFingerNumberText() {
        fingerNumberText.text = androidResourcesHelper.getString(FingerRes.get(finger).nameId)
        fingerNumberText.setTextColor(getColor(requireContext(), R.color.simprints_blue))
    }

    private fun updateFingerDirectionText() {
        if (finger.isLastFinger &&
            (finger.status == FingerStatus.GOOD_SCAN ||
                finger.status == FingerStatus.RESCAN_GOOD_SCAN)) {
            fingerDirectionText.text = androidResourcesHelper.getString(R.string.empty)
        } else {
            fingerDirectionText.text = androidResourcesHelper.getString(finger.status.textDirection)
        }
        fingerDirectionText.setTextColor(finger.status.textDirectionColor)
    }

    companion object {

        private const val FINGER_ARG = "finger"

        fun newInstance(
            finger: Finger,
            androidResourcesHelper: FingerprintAndroidResourcesHelper,
            fingerprintPreferencesManager: FingerprintPreferencesManager
        ): FingerFragment {
            val fingerFragment = FingerFragment()
            val bundle = Bundle()
            bundle.putParcelable(FINGER_ARG, finger)
            fingerFragment.arguments = bundle
            fingerFragment.androidResourcesHelper = androidResourcesHelper
            fingerFragment.fingerprintPreferencesManager = fingerprintPreferencesManager
            return fingerFragment
        }
    }
}
