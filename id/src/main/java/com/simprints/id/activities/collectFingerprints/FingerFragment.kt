package com.simprints.id.activities.collectFingerprints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.simprints.id.R
import com.simprints.id.domain.Finger
import com.simprints.id.domain.FingerRes
import com.simprints.id.tools.extensions.activityIsPresentAndFragmentIsAdded
import kotlinx.android.synthetic.main.fragment_finger.*

class FingerFragment : Fragment() {

    lateinit var finger: Finger

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger, container, false)

        finger = arguments?.get(FINGER_ARG) as Finger

        FingerRes.setFingerRes()

        if(activityIsPresentAndFragmentIsAdded()) {
            updateFingerImageAccordingToStatus()
            updateTextAccordingToStatus()
        }

        return view
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
        fingerResultText.setText(finger.status.textResult)
        fingerResultText.setTextColor(getColor(requireContext(), finger.status.textResultColorRes))
    }

    private fun updateFingerNumberText() {
        fingerNumberText.text = getString(FingerRes.get(finger).nameId)
        fingerNumberText.setTextColor(getColor(requireContext(), R.color.simprints_blue))
    }

    private fun updateFingerDirectionText() {
        if (finger.isLastFinger &&
            (finger.status == Finger.Status.GOOD_SCAN ||
                finger.status == Finger.Status.RESCAN_GOOD_SCAN)) {
            fingerDirectionText.setText(R.string.empty)
        } else {
            fingerDirectionText.setText(finger.status.textDirection)
        }
        fingerDirectionText.setTextColor(finger.status.textDirectionColor)
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
