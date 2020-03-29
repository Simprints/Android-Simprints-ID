package com.simprints.fingerprint.activities.connect.issues.nfcpair

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simprints.fingerprint.R
import kotlinx.android.synthetic.main.fragment_nfc_pair.*

class NfcPairFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_nfc_pair, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        couldNotPairTextView.paintFlags = couldNotPairTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        couldNotPairTextView.setOnClickListener { goToSerialEntryPair() }
    }

    private fun goToSerialEntryPair() {
        findNavController().navigate(R.id.action_nfcPairFragment_to_serialEntryPairFragment)
    }
}
